package au.edu.rmit.bdp.clustering.mapreduce;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Scanner;

import au.edu.rmit.bdp.clustering.model.Centroid;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

//import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.rmit.bdp.clustering.model.DataPoint;


public class KMeansClusteringJob {

	static double minLon;
	static double maxLon;
	static double minLat;
	static double maxLat;

	private static final Log LOG = LogFactory.getLog(KMeansClusteringJob.class);

	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		System.out.println("HOOOOOO");
		long start = System.nanoTime();
		int iteration = 1;
		Configuration conf = new Configuration();
		conf.set("num.iteration", iteration + "");
		System.out.println("HOOOOOO");
		Path firstInput = new Path(args[0] + "/22mb.txt");
		Path firstOutput = new Path(args[1]);
		Path PointDataPath = new Path(args[0] + "/data.seq");
		Path centroidDataPath = new Path(args[0] + "/centroid.seq");
		conf.set("centroid.path", centroidDataPath.toString());
		Path outputDir = new Path(args[1] + "/depth_1");

		Job job = Job.getInstance(conf);
		job.setJobName("KMeans Clustering");

		job.setMapperClass(KMeansMapper.class);
		job.setReducerClass(KMeansReducer.class);
		job.setJarByClass(KMeansMapper.class);

		FileInputFormat.addInputPath(job, PointDataPath);
//		FileSystem fs = FileSystem.get(conf);
		FileSystem fs = FileSystem.get(URI.create("s3://sepibucket"), conf);

		if (fs.exists(outputDir)) {
			fs.delete(outputDir, true);
		}

		if (fs.exists(centroidDataPath)) {
			fs.delete(centroidDataPath, true);
		}

		if (fs.exists(PointDataPath)) {
			fs.delete(PointDataPath, true);
		}

		generateCentroid(conf, centroidDataPath, fs);
		generateDataPoints(conf, PointDataPath, fs, firstInput);

		job.setNumReduceTasks(1);
		FileOutputFormat.setOutputPath(job, outputDir);
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
//		job.setInputFormatClass(TextInputFormat.class);
//		job.setOutputFormatClass(TextOutputFormat.class);

		job.setOutputKeyClass(Centroid.class);
		job.setOutputValueClass(DataPoint.class);

		job.waitForCompletion(true);

		long counter = job.getCounters().findCounter(KMeansReducer.Counter.CONVERGED).getValue();
		iteration++;
		while (counter > 0) {
			conf = new Configuration();
			conf.set("centroid.path", centroidDataPath.toString());
			conf.set("num.iteration", iteration + "");
			job = Job.getInstance(conf);
			job.setJobName("KMeans Clustering " + iteration);

			job.setMapperClass(KMeansMapper.class);
//			job.setCombinerClass(KMeansCombiner.class);
			job.setReducerClass(KMeansReducer.class);
			job.setJarByClass(KMeansMapper.class);

			PointDataPath = new Path(args[1] + "/depth_" + (iteration - 1) + "/");
			outputDir = new Path(args[1] + "/depth_" + iteration);

			FileInputFormat.addInputPath(job, PointDataPath);
			if (fs.exists(outputDir))
				fs.delete(outputDir, true);

			FileOutputFormat.setOutputPath(job, outputDir);
			job.setInputFormatClass(SequenceFileInputFormat.class);
			job.setOutputFormatClass(SequenceFileOutputFormat.class);
//			job.setInputFormatClass(TextInputFormat.class);
//			job.setOutputFormatClass(TextOutputFormat.class);

			job.setOutputKeyClass(Centroid.class);
			job.setOutputValueClass(DataPoint.class);
			job.setNumReduceTasks(1);

			job.waitForCompletion(true);
			iteration++;
			counter = job.getCounters().findCounter(KMeansReducer.Counter.CONVERGED).getValue();
		}

		Path result = new Path(args[1] + "/depth_" + (iteration - 1) + "/");
		LOG.info("OUTPUT IS IN HDFS");
		FSDataOutputStream outputStream = fs.create(new Path(firstOutput + "/output.txt"));
//		PrintWriter pw = new PrintWriter(outputStream);
		FileStatus[] stati = fs.listStatus(result);
		for (FileStatus status : stati) {
			if (!status.isDirectory()) {
				Path path = status.getPath();
				if (!path.getName().equals("_SUCCESS")) {
					LOG.info("FOUND " + path.toString());
//					try (SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf)) {
					try (Reader reader = new Reader(conf, Reader.file(path))) {
						Centroid key = new Centroid();
						DataPoint v = new DataPoint();
						while (reader.next(key, v)) {
							LOG.info(key + "," + v);
							outputStream.writeBytes(key + "," + v + "\n");
//							pw.write(key + "," + v + "\n");
						}
						reader.close();
//						pw.close();
						outputStream.close();
					}
				}
			}
		}
		long end = System.nanoTime();
		long timez = (end - start) / 1000000000;
		System.out.println(timez + " Seconds");
	}

	public static void generateDataPoints(Configuration conf, Path in, FileSystem fs, Path input) throws IOException {
		try (SequenceFile.Writer dataWriter = SequenceFile.createWriter(conf, SequenceFile.Writer.file(in),
				SequenceFile.Writer.keyClass(Centroid.class), SequenceFile.Writer.valueClass(DataPoint.class))) {

			System.out.println("HI GENERATE DATAPOINT IS RUNNING!!!");
			try (Scanner sc = new Scanner(new InputStreamReader(fs.open(input)))) {
				System.out.println("YAAAAAAYYYYY");

				while (sc.hasNextLine()) {
					String line = sc.nextLine();
					String[] splt = line.split(",");
					if (splt.length < 1) {
						break;
					}
					//System.out.println("HAAAYYYY!!!");

					dataWriter.append(new Centroid(new DataPoint(0, 0)),
							new DataPoint(Double.parseDouble(splt[0]), Double.parseDouble(splt[1])));
					line = sc.nextLine();

				}
			} catch (Exception e) {
				System.err.println("An Error Occurred:\n" + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static void generateCentroid(Configuration conf, Path center, FileSystem fs) throws IOException {
//		try (SequenceFile.Writer centerWriter = SequenceFile.createWriter(fs, conf, center, Centroid.class,
//				IntWritable.class)) {
		try (SequenceFile.Writer centerWriter = SequenceFile.createWriter(conf, SequenceFile.Writer.file(center),
				SequenceFile.Writer.keyClass(Centroid.class), SequenceFile.Writer.valueClass(DataPoint.class))) {
			System.out.println("HI Generate Centroid. Value class: " + centerWriter.getValueClass());
			DataPoint value = new DataPoint(0);
			centerWriter.append(new Centroid(new DataPoint(-73.96082, 40.78020)), value);
			centerWriter.append(new Centroid(new DataPoint(-73.83872, 40.72636)), value);
			centerWriter.append(new Centroid(new DataPoint(-73.98090, 40.75726)), value);
			centerWriter.append(new Centroid(new DataPoint(-73.99725, 40.72802)), value);
			centerWriter.append(new Centroid(new DataPoint(-74.01525, 40.71609)), value);
			centerWriter.append(new Centroid(new DataPoint(-74.00766, 40.74083)), value);

		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("ERROR: \n" + e.getMessage());
			e.printStackTrace();
		}
	}
}

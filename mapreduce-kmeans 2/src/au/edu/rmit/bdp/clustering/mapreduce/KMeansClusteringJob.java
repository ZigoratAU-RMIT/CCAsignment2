package au.edu.rmit.bdp.clustering.mapreduce;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import au.edu.rmit.bdp.clustering.model.Centroid;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.commons.httpclient.URI;

//import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.rmit.bdp.clustering.model.DataPoint;

/**
 * K-means algorithm in mapReduce
 * <p>
 *
 * Terminology explained: - DataPoint: A dataPoint is a point in 2 dimensional
 * space. we can have as many as points we want, and we are going to group those
 * points that are similar( near) to each other. - cluster: A cluster is a group
 * of dataPoints that are near to each other. - Centroid: A centroid is the
 * center point( not exactly, but you can think this way at first) of the
 * cluster.
 *
 * Files involved: - data.seq: It contains all the data points. Each chunk
 * consists of a key( a dummy centroid) and a value(data point). - centroid.seq:
 * It contains all the centroids with random initial values. Each chunk consists
 * of a key( centroid) and a value( a dummy int) - depth_*.seq: These are a set
 * of directories( depth_1.seq, depth_2.seq, depth_3.seq ... ), each of the
 * directory will contain the result of one job. Note that the algorithm works
 * iteratively. It will keep creating and executing the job before all the
 * centroid converges. each of these directory contains files which is produced
 * by reducer of previous round, and it is going to be fed to the mapper of next
 * round. Note, these files are binary files, and they follow certain protocals
 * so that they can be serialized and deserialized by SequenceFileOutputFormat
 * and SequenceFileInputFormat
 *
 * This is an high level demonstration of how this works:
 *
 * - We generate some data points and centroids, and write them to data.seq and
 * cen.seq respectively. We use SequenceFile.Writer so that the data could be
 * deserialize easily.
 *
 * - We start our first job, and feed data.seq to it, the output of reducer
 * should be in depth_1.seq. cen.seq file is also updated in reducer#cleanUp. -
 * From our second job, we keep generating new job and feed it with previous
 * job's output( depth_1.seq/ in this case), until all centroids converge.
 *
 */
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
		
		
		Path firstInput = new Path(args[0]);
		Path firstOutput = new Path(args[1]);
		Path PointDataPath = new Path("clustering/data.seq");
		Path centroidDataPath = new Path("clustering/centroid.seq");
		conf.set("centroid.path", centroidDataPath.toString());
		Path outputDir = new Path("clustering/depth_1");

		Job job = Job.getInstance(conf);
		job.setJobName("KMeans Clustering");

		job.setMapperClass(KMeansMapper.class);
//		job.setCombinerClass(KMeansCombiner.class);
		job.setReducerClass(KMeansReducer.class);
		job.setJarByClass(KMeansMapper.class);

		FileInputFormat.addInputPath(job, PointDataPath);
		FileSystem fs = FileSystem.get(conf);

//		conf.set("fs.s3a.impl", classOf[org.apache.hadoop.fs.s3a.S3FileSystem].getName);
//		java.net.URI uri = new java.net.URI("s3://sepibucket");
////		
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

			PointDataPath = new Path("clustering/depth_" + (iteration - 1) + "/");
			outputDir = new Path("clustering/depth_" + iteration);

			FileInputFormat.addInputPath(job, PointDataPath);
			if (fs.exists(outputDir))
				fs.delete(outputDir, true);

			FileOutputFormat.setOutputPath(job, outputDir);
			job.setInputFormatClass(SequenceFileInputFormat.class);
			job.setOutputFormatClass(SequenceFileOutputFormat.class);
			job.setOutputKeyClass(Centroid.class);
			job.setOutputValueClass(DataPoint.class);
			job.setNumReduceTasks(1);

			job.waitForCompletion(true);
			iteration++;
			counter = job.getCounters().findCounter(KMeansReducer.Counter.CONVERGED).getValue();
		}

		Path result = new Path("clustering/depth_" + (iteration - 1) + "/");
		// Configuration config = new Configuration();
		// FileSystem fisys = FileSystem.get(config);
		LOG.info("OUTPUT IS IN HDFS");
		FSDataOutputStream outputStream = fs.create(firstOutput);

		FileStatus[] stati = fs.listStatus(result);
		for (FileStatus status : stati) {
			if (!status.isDirectory()) {
				Path path = status.getPath();
				if (!path.getName().equals("_SUCCESS")) {
					LOG.info("FOUND " + path.toString());
//					try (SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf)) {
					try (SequenceFile.Reader reader = new SequenceFile.Reader(conf, SequenceFile.Reader.file(path),
							SequenceFile.Reader.bufferSize(4096))) {
						Centroid key = new Centroid();
						DataPoint v = new DataPoint();
						while (reader.next(key, v)) {
							LOG.info(key + " | " + v);
							outputStream.writeBytes(key + " | " + v + "\n");
						}
						reader.close();
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
//			dataWriter.append(new Centroid(new DataPoint(0, 0)), new DataPoint(1, 2));
//			dataWriter.append(new Centroid(new DataPoint(0, 0)), new DataPoint(16, 3));
//			dataWriter.append(new Centroid(new DataPoint(0, 0)), new DataPoint(3, 3));
//			dataWriter.append(new Centroid(new DataPoint(0, 0)), new DataPoint(2, 2));
//			dataWriter.append(new Centroid(new DataPoint(0, 0)), new DataPoint(2, 3));
//			dataWriter.append(new Centroid(new DataPoint(0, 0)), new DataPoint(25, 1));
//			dataWriter.append(new Centroid(new DataPoint(0, 0)), new DataPoint(7, 6));
//			dataWriter.append(new Centroid(new DataPoint(0, 0)), new DataPoint(6, 5));
//			dataWriter.append(new Centroid(new DataPoint(0, 0)), new DataPoint(-1, -23));
			System.out.println("HI GENERATE DATAPOINT IS RUNNING!!!");
			// fs = FileSystem.get(conf);
			try (Scanner sc = new Scanner(new InputStreamReader(fs.open(input)))) {

				while (sc.hasNextLine()) {
					String line = sc.nextLine();
					String[] splt = line.split(",");
					if (splt.length < 1) {
						break;
					}
//					for (int i = 0; i < splt.length; i++) {
//						List<Double> lon = new ArrayList<>();
//						List<Double> lat = new ArrayList<>();
////						lon = new ArrayList<>();
////						lat = new ArrayList<>();
//						lon.add(Double.parseDouble(splt[5]));
//						lat.add(Double.parseDouble(splt[6]));
//						System.out.println("HEEEEEEEEEEEEEEEEEYYYYYYYY!!!!****" + splt.length);
//						minLon = Collections.min(lon);
//						maxLon = Collections.max(lon);
//						minLat = Collections.min(lat);
//						maxLat = Collections.max(lat);
//					}

					dataWriter.append(new Centroid(new DataPoint(0, 0)),
							new DataPoint(Double.parseDouble(splt[0]), Double.parseDouble(splt[1])));
					line = sc.nextLine();

//					System.out.println(Double.parseDouble(splt[5]) + "," + Double.parseDouble(splt[6]));
				}
			} catch (Exception e) {
				System.out.println("An error occured:\n" + e.getMessage());
			}
		}
	}

	public static void generateCentroid(Configuration conf, Path center, FileSystem fs) throws IOException {
//		try (SequenceFile.Writer centerWriter = SequenceFile.createWriter(fs, conf, center, Centroid.class,
//				IntWritable.class)) {
		try (SequenceFile.Writer centerWriter = SequenceFile.createWriter(conf, SequenceFile.Writer.file(center),
				SequenceFile.Writer.keyClass(Centroid.class), SequenceFile.Writer.valueClass(IntWritable.class))) {
			System.out.println("HI Generate Centroid is Running!!!");
			final IntWritable value = new IntWritable(0);
			centerWriter.append(new Centroid(new DataPoint(-73.96082, 40.78020)), value);
			centerWriter.append(new Centroid(new DataPoint(-73.83872, 40.72636)), value);
			centerWriter.append(new Centroid(new DataPoint(-73.98090, 40.75726)), value);
			centerWriter.append(new Centroid(new DataPoint(-73.99725, 40.72802)), value);

//			centerWriter.append(new Centroid(new DataPoint(minLon, minLat)), value);
//			centerWriter.append(new Centroid(new DataPoint(maxLon, maxLat)), value);
//			System.out.println(minLon + "*,*" + minLat + "*,*" + maxLon + "*,*" + maxLat);

//			LOG.info(randLong);
//			for (int i = 0; i <= 1; i++) {
//				enterWriter.append(new Centroid(new DataPoint(randLong, randLat)), value);
//				LOG.info(randLong);
//				LOG.info(randLat);
//			}

//			double lon1 = -73.00;
//			double lon2 = -75.00;
//			double lat1 = 42.00;
//			double lat2 = 40.00;
//
//			Random rand = new Random();
//
//			for (int i = 0; i <= 1; i++) {
//				double randLong = lon2 + (lon1 - lon2) * rand.nextDouble();
//				double randLat = lat2 + (lat1 - lat2) * rand.nextDouble();
//				centerWriter.append(new Centroid(new DataPoint(randLong, randLat)), value);
//				LOG.info(randLong);
//				LOG.info(randLat);
//			}
//			centerWriter.append(
//					new Centroid(new DataPoint(getRandomNuberInRange(lon2, lon1), getRandomNuberInRange(lat1, lat2))),
//					value);
//			centerWriter.append(
//					new Centroid(new DataPoint(getRandomNuberInRange(lon2, lon1), getRandomNuberInRange(lat1, lat2))),
//					value);

			// centerWriter.append(new Centroid(new DataPoint(1, 1)), value);
//			centerWriter.append(new Centroid(new DataPoint(80, 10)), value);
		}
	}
//
//	private static double getRandomNuberInRange(double min, double max) {
//		return rand.doubles(min, (max + 1)).limit(1).findFirst().getAsDouble();
//
//	}

}

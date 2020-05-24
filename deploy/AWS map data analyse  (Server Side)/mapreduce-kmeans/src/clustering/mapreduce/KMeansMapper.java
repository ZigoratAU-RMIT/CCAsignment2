package au.edu.rmit.bdp.clustering.mapreduce;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import au.edu.rmit.bdp.distance.DistanceMeasurer;
import au.edu.rmit.bdp.distance.EuclidianDistance;
import au.edu.rmit.bdp.clustering.model.Centroid;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapreduce.Mapper;

import au.edu.rmit.bdp.clustering.model.DataPoint;


public class KMeansMapper extends Mapper<Centroid, DataPoint, Centroid, DataPoint> {

	private final List<Centroid> centers = new ArrayList<>();
	private DistanceMeasurer distanceMeasurer;

//	@SuppressWarnings("deprecation")
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		super.setup(context);

		// We get the URI to the centroid file on hadoop file system (not local fs!).
		// The url is set beforehand in KMeansClusteringJob#main.
		Configuration conf = context.getConfiguration();
		Path centroids = new Path(conf.get("centroid.path"));
//		FileSystem fs = FileSystem.get(conf);
		FileSystem fs = FileSystem.get(URI.create("s3://sepibucket"), conf);

		
		try (SequenceFile.Reader reader = new SequenceFile.Reader(conf, SequenceFile.Reader.file(centroids),
				SequenceFile.Reader.bufferSize(4096))) {
//		try (SequenceFile.Reader reader = new SequenceFile.Reader(fs, centroids, conf)) {
			Centroid key = new Centroid();
			DataPoint value = new DataPoint();
			int index = 0;
			while (reader.next(key, value)) {
				Centroid centroid = new Centroid(key);
				centroid.setClusterIndex(index++);
				centers.add(centroid);
			}
		}

		// This is for calculating the distance between a point and another (centroid is
		// essentially a point).
		distanceMeasurer = new EuclidianDistance();
	}

	
	@Override
	protected void map(Centroid centroid, DataPoint dataPoint, Context context)
			throws IOException, InterruptedException {
		Centroid nearest = null;
		double nearestDistance = Double.MAX_VALUE;
		for (Centroid c : centers) {
			Double dist = distanceMeasurer.measureDistance(c.getCenterVector(), dataPoint.getVector());// stores the
																										// distance
																										// value
			if (nearest != null && dist < nearestDistance) {// if the calculated value is less than the existing nearest
															// distance
				nearestDistance = dist;// give the nearest distance, the new value
				nearest = c;// and point that centroid to be the nearest centroid to that datapoint
			} else if (nearest == null) {
				nearestDistance = dist;// assigns the first nearest value
				nearest = c;
			}
		}

		context.write(nearest, dataPoint);
	}
//	@SuppressWarnings("deprecation")
//	protected void cleanup(Context context) throws IOException, InterruptedException {
//		super.cleanup(context);
//		
//
//	}
//			// todo: serialize updated centroids.
//			

}

/**
 *
 */
package com.indago.examples;

import gurobi.GRBException;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.imagej.ops.Op;
import net.imagej.ops.OpRef;
import net.imagej.ops.OpService;
import net.imagej.ops.features.DefaultAutoResolvingFeatureSet;
import net.imagej.ops.features.OpResolverService;
import net.imagej.ops.features.firstorder.FirstOrderFeatures.MeanFeature;
import net.imagej.ops.features.firstorder.FirstOrderFeatures.SumFeature;
import net.imagej.ops.features.firstorder.FirstOrderFeatures.VarianceFeature;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.roi.Regions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.scijava.Context;

import weka.classifiers.trees.RandomForest;

import com.indago.benchmarks.RandomCostBenchmarks.Parameters;
import com.indago.fg.Assignment;
import com.indago.fg.FactorGraph;
import com.indago.ilp.SolveBooleanFGGurobi;
import com.indago.segment.LabelingBuilder;
import com.indago.segment.LabelingSegment;
import com.indago.segment.RandomForestFactory;
import com.indago.segment.RandomForestSegmentCosts;
import com.indago.segment.fg.FactorGraphFactory;
import com.indago.segment.filteredcomponents.FilteredComponentTree;
import com.indago.segment.filteredcomponents.FilteredComponentTree.Filter;
import com.indago.segment.filteredcomponents.FilteredComponentTree.MaxGrowthPerStep;
import com.indago.weka.ArffBuilder;
import com.indago.weka.ArffWriterFactory;

/**
 * @author jug
 */
public class FeatureExampleOnRealSegments {

	private static String pathprefix = "src/main/resources/synthetic/0001_z63";

	public static void main(final String[] args) {

		try {
			final List< String > filenamesImgs = new ArrayList< String >();
			filenamesImgs.add( pathprefix + "/image-final_0001-z63.tif" );
			final List< Img< DoubleType >> imgs = loadImages( filenamesImgs, new DoubleType() );

			final List< String > filenamesGroundTruth = new ArrayList< String >();
			filenamesGroundTruth.add( pathprefix + "/image-labels_0001-z63.tif" );
			final List< Img< UnsignedIntType >> gt =
					loadImages( filenamesGroundTruth, new UnsignedIntType() );

			final List< String > filenamesLbls = new ArrayList< String >();
			filenamesLbls.add( pathprefix + "/ParamaxFlowSumImg.tif" );
			final List< Img< UnsignedIntType >> segments =
					loadImages( filenamesLbls, new UnsignedIntType() );

			genericMain( imgs, segments, gt, new DoubleType(), new UnsignedIntType() );
		} catch ( final ImgIOException iioe ) {
			iioe.printStackTrace();
		}
	}

	/**
	 * @return
	 * @throws ImgIOException
	 */
	public static < T extends RealType< T > & NativeType< T > > List< Img< T >>
			loadImages( final List< String > filenames, final T type ) throws ImgIOException {
		final List< Img< T >> imgs = new ArrayList< Img< T >>();

		for ( final String filename : filenames ) {
			final Img< T > img =
					new ImgOpener().openImg( filename, new ArrayImgFactory< T >(), type );
			imgs.add( img );
		}
		return imgs;
	}

	/**
	 * @param imgs
	 *            raw image data
	 * @param labelings
	 *            segment hypotheses
	 * @param gt
	 *            ground truth labels
	 * @param imgtype
	 * @param labeltype
	 */
	public static
			< T extends RealType< T > & NativeType< T >, L extends IntegerType< L > & NativeType< L > >
			void genericMain(
					final List< Img< T >> imgs,
					final List< Img< L >> labelings,
					final List< Img< L >> gt,
					final T imgtype,
					final L labeltype ) {

		// Check matching length of images and labelings
		if ( imgs.size() != labelings.size() ) { throw new IllegalArgumentException( "Given list of images and labellings must match in length!" ); }

		// create service & context
		// ------------------------

		final Context c = new Context();
		final OpResolverService ors = c.service(OpResolverService.class);
		final OpService ops = c.service(OpService.class);

		// create our own feature set
		// ------------------------

		final DefaultAutoResolvingFeatureSet< IterableInterval< T >, DoubleType > featureSet =
				new DefaultAutoResolvingFeatureSet< IterableInterval< T >, DoubleType >();
		c.inject( featureSet );

		@SuppressWarnings( "rawtypes" )
		final OpRef< MeanFeature > oprefMean =
				new OpRef< MeanFeature >( "mean", MeanFeature.class );

		@SuppressWarnings( "rawtypes" )
		final OpRef< SumFeature > oprefSum =
				new OpRef< SumFeature >( "sum", SumFeature.class );

		@SuppressWarnings( "rawtypes" )
		final OpRef< VarianceFeature > oprefVar =
				new OpRef< VarianceFeature >( "variance", VarianceFeature.class );

		featureSet.addOutputOp( oprefMean );
		featureSet.addOutputOp( oprefSum );
		featureSet.addOutputOp( oprefVar );

		// create training- and testset
		final ArffBuilder arffTrain = ArffWriterFactory.getArffBuilderFor( featureSet );
		final ArffBuilder arffTest = ArffWriterFactory.getArffBuilderFor( featureSet );

		// loop over loaded data (images)
		// ------------------------------
		for ( int i = 0; i < imgs.size(); i++ ) {
			final Img< T > img = imgs.get( i );
			final Img< L > gtImg = gt.get( i );
			final Img< L > lblImg = labelings.get( i );

			System.out.print( String.format(
					"Working on given image/labeling pair #%3d...",
					i + 1 ) );

			computeFeaturesOnAllSegments(
					img,
					gtImg,
					labeltype,
					featureSet,
					arffTrain,
					ArffBuilder.POSITIVE_INSTANCE );
			computeFeaturesOnAllSegments(
					img,
					Views.interval( Views.translate( Views.extendZero( gtImg ), 10, 10 ), gtImg ),
					labeltype,
					featureSet,
					arffTrain,
					ArffBuilder.NEGATIVE_INSTANCE );

			computeFeaturesOnAllSegments(
					img,
					lblImg,
					labeltype,
					featureSet,
					arffTest,
					ArffBuilder.UNKNOWN_INSTANCE );
		}

		try {
			arffTrain.write( pathprefix + "/FeatureExampleOnRealSegments_traindata.arff" );
			arffTest.write( pathprefix + "/FeatureExampleOnRealSegments_testdata.arff" );
		} catch ( final FileNotFoundException e ) {
			e.printStackTrace();
		}
		System.out.println( "...done!" );

		// -----------------------------------------------------------------------------------------------

		System.out.print( "Training random forest classifier..." );
		final RandomForest tree = new RandomForest();
		try {
			tree.buildClassifier( arffTrain.getData() );
		} catch ( final Exception e ) {
			e.printStackTrace();
			System.exit( 1 );
		}
		System.out.println( "...done!" );

		// -----------------------------------------------------------------------------------------------

		System.out.println( "Applying trained random forest classifier to each test-image..." );
		for ( int i = 0; i < imgs.size(); i++ ) {
			final Img< T > img = imgs.get( i );
			final Img< L > lblImg = labelings.get( i );

			final RandomForestSegmentCosts< L, T > costs =
					new RandomForestSegmentCosts< L, T >( lblImg, img, tree, featureSet, labeltype );

			System.out.print( "\tOptimum search for Image " + i + ": Finding optimum..." );
			final FactorGraph fg =
					FactorGraphFactory.createFromConflictGraph(
							costs.getSegments(),
							costs.getConflictGraph(),
							costs );

			Assignment assignment = null;
			try {
				final SolveBooleanFGGurobi solver = new SolveBooleanFGGurobi();
				assignment = solver.solve( fg );
			} catch ( final GRBException e ) {
				System.err.println( "Gurobi trouble... Boolen FactorGraph could not be solved!" );
				e.printStackTrace();
			}
			System.out.println( "\t...done!" );
		}
		System.out.println( "...done!" );

		// -----------------------------------------------------------------------------------------------

	}

	/**
	 * @param img
	 * @param sumImg
	 * @param labeltype
	 * @param featureSet
	 * @param arffBuilder
	 * @param classIdentifier
	 */
	public static
			< L extends IntegerType< L > & NativeType< L >, T extends RealType< T > & NativeType< T >>
			void
			computeFeaturesOnAllSegments(
					final RandomAccessibleInterval< T > img,
					final RandomAccessibleInterval< L > sumImg,
					final L labeltype,
					final DefaultAutoResolvingFeatureSet< IterableInterval< T >, DoubleType > featureSet,
					final ArffBuilder arffBuilder,
					final String classIdentifier ) {

		final int minComponentSize = 10;
		final int maxComponentSize = 10000;
		final Filter maxGrowthPerStep = new MaxGrowthPerStep( 1 );
		final boolean darkToBright = false;
		final FilteredComponentTree< L > tree =
				FilteredComponentTree.buildComponentTree(
						sumImg,
						labeltype,
						minComponentSize,
						maxComponentSize,
						maxGrowthPerStep,
						darkToBright );

		final LabelingBuilder builder = new LabelingBuilder( sumImg );
		builder.buildLabelingForest( tree );
		final ArrayList< LabelingSegment > segments = builder.getSegments();

		for ( final LabelingSegment segment : segments ) {

			final IterableInterval< T > pixels = Regions.sample( segment.getRegion(), img );
			final Map< OpRef< ? extends Op >, DoubleType > features = featureSet.compute( pixels );

			arffBuilder.addData( features, classIdentifier );
		}
	}

	public static ArrayList< Img< UnsignedIntType >> generateRandomSyntheticImgs( final int numImgs, final int seed, final Parameters p ) {

		final ArrayList< Img< UnsignedIntType >> imgs = new ArrayList< Img< UnsignedIntType >>();
		for ( int f = 0; f < numImgs; f++ ) {
			imgs.add( RandomForestFactory.getForestImg( p.width, p.height, p.numSeedPixels, p.maxRadius, p.minDeltaR, p.maxDeltaR, p.meanDeltaR, p.sdDeltaR, p.minIntensity, p.maxIntensity, seed + f ) );
		}

		return imgs;
	}
}
/**
 *
 */
package com.jug.indago.influit.nodes;

import java.util.List;

import com.jug.indago.influit.data.InfluitDatum;
import com.jug.indago.influit.exception.InfluitFormatException;


/**
 * @author jug
 */
public class FilteredComponentTreeNode implements InfluitNode {

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FilteredComponentTreeNode";
	}

	/**
	 * @see com.jug.indago.influit.nodes.InfluitNode#getSupportedInputFormats()
	 */
	@Override
	public List< InfluitDatum > getSupportedInputFormats() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.jug.indago.influit.nodes.InfluitNode#getSupportedOutputFormats()
	 */
	@Override
	public List< InfluitDatum > getSupportedOutputFormats() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.jug.indago.influit.nodes.InfluitNode#canEvaluate()
	 */
	@Override
	public boolean canEvaluate() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see com.jug.indago.influit.nodes.InfluitNode#evaluate()
	 */
	@Override
	public void evaluate() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.jug.indago.influit.nodes.InfluitNode#getOutput(com.jug.indago.influit.data.InfluitDatum)
	 */
	@Override
	public void getOutput( final InfluitDatum data ) throws InfluitFormatException {
		// TODO Auto-generated method stub

	}

}

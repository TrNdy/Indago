package com.indago.fg.variable;

import java.util.ArrayList;
import java.util.List;

import com.indago.fg.domain.BooleanDomain;
import com.indago.fg.factor.Factor;

public class BooleanVariable implements Variable< BooleanDomain > {

	private final List< Factor< BooleanDomain, ?, ? >> factors = new ArrayList< Factor< BooleanDomain, ?, ? >>();

	private final BooleanDomain domain;

	public BooleanVariable() {
		this( BooleanDomain.get() );
	}

	public BooleanVariable( final BooleanDomain domain ) {
		this.domain = domain;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	/**
	 * @see com.indago.fg.variable.Variable#getFactors()
	 */
	@Override
	public List< ? extends Factor< BooleanDomain, ?, ? >> getFactors() {
		return factors;
	}

	/**
	 * @see com.indago.fg.variable.Variable#addFactor(com.indago.fg.factor.Factor)
	 */
	@Override
	public void addFactor( final Factor< BooleanDomain, ?, ? > factor ) {
		factors.add( factor );
	}

	/**
	 * @see com.indago.fg.variable.Variable#getFactors()
	 */
	@Override
	public List< ? extends Factor< BooleanDomain, ?, ? >> getFactors() {
		return factors;
	}

	/**
	 * @see com.indago.fg.variable.Variable#addFactor(com.indago.fg.factor.Factor)
	 */
	@Override
	public void addFactor( final Factor< BooleanDomain, ?, ? > factor ) {
		factors.add( factor );
	}
}

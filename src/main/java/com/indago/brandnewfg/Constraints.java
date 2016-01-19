package com.indago.brandnewfg;

import static com.indago.brandnewfg.Relation.LE;

import java.util.ArrayList;
import java.util.Arrays;

// TODO: not currently thread-safe
public class Constraints {

	public static LinearConstraint atMostOneConstraint( final int arity ) {
		while ( arity > atMostOneConstraints.size() - 1 ) {
			final int[] coefficients = new int[ atMostOneConstraints.size() ];
			Arrays.fill( coefficients, 1 );
			final LinearConstraint c = new LinearConstraint(
					coefficients,
					LE,
					1,
					Domains.getDefaultBinaryArgumentDomains( arity ) );
			atMostOneConstraints.add( c );
		}
		return atMostOneConstraints.get( arity );
	}

	private static final ArrayList< LinearConstraint > atMostOneConstraints = new ArrayList< >();
}

// Metawidget
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package org.metawidget.inspector.impl.propertystyle;

import java.util.regex.Pattern;

import org.metawidget.util.simple.ObjectUtils;

/**
 * Base class for BasePropertyStyle configurations.
 *
 * @author Richard Kennard
 */

// TODO: unit test config

public class BasePropertyStyleConfig
{
	//
	// Private statics
	//

	private static Pattern	DEFAULT_EXCLUDE_BASE_TYPE;

	//
	// Private members
	//

	private Pattern			mExcludeBaseType;

	private boolean			mNullExcludeBaseType;

	//
	// Public methods
	//

	/**
	 * Sets the Pattern used to exclude base types when searching up the model inheritance chain.
	 * <p>
	 * This can be useful when the base types define properties that are framework-specific, and
	 * should be filtered out from 'real' business model properties.
	 * <p>
	 * By default, excludes any base types from <code>java.*</code> or <code>javax.*</code>.
	 *
	 * @return this, as part of a fluent interface
	 */

	public BasePropertyStyleConfig setExcludeBaseType( Pattern excludeBaseType )
	{
		mExcludeBaseType = excludeBaseType;

		if ( excludeBaseType == null )
			mNullExcludeBaseType = true;

		// Fluent interface

		return this;
	}

	public Pattern getExcludeBaseType()
	{
		if ( mExcludeBaseType == null && !mNullExcludeBaseType )
		{
			if ( DEFAULT_EXCLUDE_BASE_TYPE == null )
				DEFAULT_EXCLUDE_BASE_TYPE = Pattern.compile( "^(java|javax)\\..*$" );

			mExcludeBaseType = DEFAULT_EXCLUDE_BASE_TYPE;
		}

		return mExcludeBaseType;
	}

	@Override
	public boolean equals( Object that )
	{
		if ( this == that )
			return true;

		if ( !( that instanceof BasePropertyStyleConfig ) )
			return false;

		if ( !ObjectUtils.nullSafeEquals( mExcludeBaseType, ( (BasePropertyStyleConfig) that ).mExcludeBaseType ) )
			return false;

		if ( mNullExcludeBaseType != ( (BasePropertyStyleConfig) that ).mNullExcludeBaseType )
			return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		int hashCode = 1;
		hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode( mExcludeBaseType );
		hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode( mNullExcludeBaseType );

		return hashCode;
	}
}
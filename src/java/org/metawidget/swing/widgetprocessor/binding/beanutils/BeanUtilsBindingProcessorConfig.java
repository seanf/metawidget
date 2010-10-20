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

package org.metawidget.swing.widgetprocessor.binding.beanutils;

/**
 * Configures a BeanUtilsBindingProcessor prior to use. Once instantiated, WidgetProcessors are
 * immutable.
 *
 * @author Richard Kennard
 */

public class BeanUtilsBindingProcessorConfig {

	//
	// Public statics
	//

	public static final int	PROPERTYSTYLE_JAVABEAN	= 0;

	public static final int	PROPERTYSTYLE_SCALA		= 1;

	//
	// Private members
	//

	private int				mPropertyStyle			= PROPERTYSTYLE_JAVABEAN;

	//
	// Public methods
	//

	/**
	 * Sets the PropertyStyle for this BeanUtilsBinding.
	 *
	 * @return this, as part of a fluent interface
	 */

	public BeanUtilsBindingProcessorConfig setPropertyStyle( int propertyStyle ) {

		mPropertyStyle = propertyStyle;

		return this;
	}

	@Override
	public boolean equals( Object that ) {

		if ( this == that ) {
			return true;
		}

		if ( that == null ) {
			return false;
		}

		if ( getClass() != that.getClass() ) {
			return false;
		}

		if ( mPropertyStyle != ( (BeanUtilsBindingProcessorConfig) that ).mPropertyStyle ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {

		return mPropertyStyle;
	}

	//
	// Protected methods
	//

	protected int getPropertyStyle() {

		return mPropertyStyle;
	}
}

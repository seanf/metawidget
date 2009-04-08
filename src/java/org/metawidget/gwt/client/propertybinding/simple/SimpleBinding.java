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

package org.metawidget.gwt.client.propertybinding.simple;

import static org.metawidget.inspector.InspectionResultConstants.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.metawidget.gwt.client.actionbinding.ActionBinding;
import org.metawidget.gwt.client.propertybinding.BasePropertyBinding;
import org.metawidget.gwt.client.ui.GwtMetawidget;
import org.metawidget.gwt.client.ui.Stub;
import org.metawidget.util.simple.PathUtils;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Simple, Generator-based property and action binding implementation.
 *
 * @author Richard Kennard
 */

public class SimpleBinding
	extends BasePropertyBinding
	implements ActionBinding
{
	//
	// Private statics
	//

	private final static Map<Class<?>, SimpleBindingAdapter<?>>	ADAPTERS	= new HashMap<Class<?>, SimpleBindingAdapter<?>>();

	private final static Map<Class<?>, Converter<?>>			CONVERTERS	= new HashMap<Class<?>, Converter<?>>();

	static
	{
		// Register default converters

		Converter<?> simpleConverter = new SimpleConverter();

		@SuppressWarnings( "unchecked" )
		Converter<Boolean> booleanConverter = (Converter<Boolean>) simpleConverter;
		registerConverter( Boolean.class, booleanConverter );

		@SuppressWarnings( "unchecked" )
		Converter<Character> characterConverter = (Converter<Character>) simpleConverter;
		registerConverter( Character.class, characterConverter );

		@SuppressWarnings( "unchecked" )
		Converter<Number> numberConverter = (Converter<Number>) simpleConverter;
		registerConverter( Number.class, numberConverter );
	}

	//
	// Public statics
	//

	/**
	 * Registers the given SimpleBindingAdapter for the given Class.
	 * <p>
	 * Adapters also apply to subclasses of the given Class. So for example registering an Adapter
	 * for <code>Contact.class</code> will match <code>PersonalContact.class</code>,
	 * <code>BusinessContact.class</code> etc., unless a more subclass-specific Adapter is also
	 * registered
	 */

	public static <T> void registerAdapter( Class<T> forClass, SimpleBindingAdapter<T> adapter )
	{
		ADAPTERS.put( forClass, adapter );
	}

	/**
	 * Registers the given Converter for the given Class.
	 * <p>
	 * Converters also apply to subclasses of the given Class. So for example registering a
	 * Converter for <code>Number.class</code> will match <code>Integer.class</code>,
	 * <code>Double.class</code> etc., unless a more subclass-specific Converter is also
	 * registered.
	 */

	public static <T> void registerConverter( Class<T> forClass, Converter<T> converter )
	{
		CONVERTERS.put( forClass, converter );
	}

	//
	// Private members
	//

	private Set<Object[]>	mBindings;

	//
	// Constructor
	//

	public SimpleBinding( GwtMetawidget metawidget )
	{
		super( metawidget );
	}

	//
	// Public methods
	//

	public void bindProperty( Widget widget, Map<String, String> attributes, String path )
	{
		// SimpleBinding doesn't bind to Stubs or FlexTables

		if ( widget instanceof Stub || widget instanceof FlexTable )
			return;

		Object toInspect = getMetawidget().getToInspect();

		if ( toInspect == null )
			return;

		// From the adapter...

		Class<?> classToBindTo = toInspect.getClass();
		SimpleBindingAdapter<Object> adapter = getAdapter( classToBindTo );

		if ( adapter == null )
			throw new RuntimeException( "Don't know how to bind to a " + classToBindTo );

		// ...fetch the value...

		String[] names = PathUtils.parsePath( path ).getNamesAsArray();
		Object value = adapter.getProperty( toInspect, names );

		// ...convert it (if necessary)...

		Class<?> propertyType = adapter.getPropertyType( toInspect, names );
		Converter<Object> converter = getConverter( propertyType );

		if ( converter != null )
			value = converter.convertForWidget( widget, value );

		// ...and set it

		try
		{
			getMetawidget().setValue( value, widget );

			if ( TRUE.equals( attributes.get( NO_SETTER ) ) )
				return;

			if ( mBindings == null )
				mBindings = new HashSet<Object[]>();

			mBindings.add( new Object[] { widget, names, converter, propertyType } );
		}
		catch ( Exception e )
		{
			Window.alert( path + ": " + e.getMessage() );
		}
	}

	public void bindAction( Widget widget, Map<String, String> attributes, String path )
	{
		// SimpleBinding doesn't bind to Stubs or FlexTables

		if ( widget instanceof Stub )
			return;

		// How can we bind without addClickListener?

		if ( !( widget instanceof FocusWidget ) )
			throw new RuntimeException( "SimpleBinding only supports binding actions to FocusWidgets - '" + attributes.get( NAME ) + "' is using a " + widget.getClass().getName() );

		// Bind the action

		final GwtMetawidget metawidget = getMetawidget();
		final String[] names = PathUtils.parsePath( path ).getNamesAsArray();

		FocusWidget focusWidget = (FocusWidget) widget;
		focusWidget.addClickListener( new ClickListener()
		{
			public void onClick( Widget sender )
			{
				// Use the adapter...

				Object toInspect = metawidget.getToInspect();

				if ( toInspect == null )
					return;

				Class<?> classToBindTo = toInspect.getClass();
				SimpleBindingAdapter<Object> adapter = getAdapter( classToBindTo );

				if ( adapter == null )
					throw new RuntimeException( "Don't know how to bind to a " + classToBindTo );

				// ...to invoke the action

				adapter.invokeAction( toInspect, names );
			}
		} );

	}

	public void rebindProperties()
	{
		if ( mBindings == null )
			return;

		GwtMetawidget metawidget = getMetawidget();
		Object toInspect = metawidget.getToInspect();

		if ( toInspect == null )
			return;

		// From the adapter...

		Class<?> classToBindTo = toInspect.getClass();
		SimpleBindingAdapter<Object> adapter = getAdapter( classToBindTo );

		if ( adapter == null )
			throw new RuntimeException( "Don't know how to rebind to a " + classToBindTo );

		// ...for each bound property...

		for ( Object[] binding : mBindings )
		{
			Widget widget = (Widget) binding[0];
			String[] names = (String[]) binding[1];
			@SuppressWarnings( "unchecked" )
			Converter<Object> converter = (Converter<Object>) binding[2];

			// ...fetch the value...

			Object value = adapter.getProperty( toInspect, names );

			// ...convert it (if necessary)...

			if ( converter != null )
				value = converter.convertForWidget( widget, value );

			// ...and set it

			metawidget.setValue( value, widget );
		}
	}

	public void saveProperties()
	{
		if ( mBindings == null )
			return;

		Object toInspect = getMetawidget().getToInspect();

		if ( toInspect == null )
			return;

		// From the adapter...

		Class<?> classToBindTo = toInspect.getClass();
		SimpleBindingAdapter<Object> adapter = getAdapter( classToBindTo );

		if ( adapter == null )
			throw new RuntimeException( "Don't know how to save to a " + classToBindTo );

		GwtMetawidget metawidget = getMetawidget();

		// ...for each bound property...

		for ( Object[] binding : mBindings )
		{
			Widget widget = (Widget) binding[0];
			String[] names = (String[]) binding[1];
			@SuppressWarnings( "unchecked" )
			Converter<Object> converter = (Converter<Object>) binding[2];
			Class<?> type = (Class<?>) binding[3];

			// ...fetch the value...

			Object value = metawidget.getValue( widget );

			// ...convert it (if necessary)...

			if ( value != null && converter != null )
				value = converter.convertFromWidget( widget, value, type );

			// ...and set it

			adapter.setProperty( toInspect, value, names );
		}
	}

	//
	// Protected methods
	//

	/**
	 * Gets the Adapter for the given class (if any).
	 * <p>
	 * Includes traversing superclasses of the given Class for a suitable Converter, so for example
	 * registering an Adapter for <code>Contact.class</code> will match
	 * <code>PersonalContact.class</code>, <code>BusinessContact.class</code> etc., unless a
	 * more subclass-specific Adapter is also registered.
	 */

	protected <T extends SimpleBindingAdapter<?>> T getAdapter( Class<?> classToBindTo )
	{
		Class<?> classTraversal = classToBindTo;

		while ( classTraversal != null )
		{
			@SuppressWarnings( "unchecked" )
			T adapter = (T) ADAPTERS.get( classTraversal );

			if ( adapter != null )
				return adapter;

			classTraversal = classTraversal.getSuperclass();
		}

		return null;
	}

	//
	// Private methods
	//

	/**
	 * Gets the Converter for the given Class (if any).
	 * <p>
	 * Includes traversing superclasses of the given Class for a suitable Converter, so for example
	 * registering a Converter for <code>Number.class</code> will match <code>Integer.class</code>,
	 * <code>Double.class</code> etc., unless a more subclass-specific Converter is also
	 * registered.
	 */

	private <T extends Converter<?>> T getConverter( Class<?> classToConvert )
	{
		Class<?> classTraversal = classToConvert;

		while ( classTraversal != null )
		{
			@SuppressWarnings( "unchecked" )
			T converter = (T) CONVERTERS.get( classTraversal );

			if ( converter != null )
				return converter;

			classTraversal = classTraversal.getSuperclass();
		}

		return null;
	}
}

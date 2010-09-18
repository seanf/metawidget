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

package org.metawidget.faces.component.layout;

import static org.metawidget.inspector.InspectionResultConstants.*;

import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;

import org.metawidget.faces.component.UIMetawidget;
import org.metawidget.faces.component.UIStub;
import org.metawidget.layout.decorator.LayoutDecoratorConfig;
import org.metawidget.util.CollectionUtils;
import org.metawidget.util.LayoutUtils;

/**
 * Convenience base class for LayoutDecorators wishing to decorate widgets based on changing
 * sections within JSF Layouts.
 *
 * @author Richard Kennard
 */

public abstract class UIComponentNestedSectionLayoutDecorator
	extends org.metawidget.layout.decorator.NestedSectionLayoutDecorator<UIComponent, UIComponent, UIMetawidget> {

	//
	// Private static
	//

	private final static String	COMPONENT_ATTRIBUTE_SECTION_WIDGET_TO_RETURN	= "section-widget-to-return";

	//
	// Constructor
	//

	protected UIComponentNestedSectionLayoutDecorator( LayoutDecoratorConfig<UIComponent, UIComponent, UIMetawidget> config ) {

		super( config );
	}

	//
	// Protected methods
	//

	@Override
	protected String stripSection( Map<String, String> attributes ) {

		return LayoutUtils.stripSection( attributes );
	}

	@Override
	protected State<UIComponent> getState( UIComponent container, UIMetawidget metawidget ) {

		@SuppressWarnings( "unchecked" )
		Map<UIComponent, State> stateMap = (Map<UIComponent, State>) metawidget.getClientProperty( getClass() );

		if ( stateMap == null ) {
			stateMap = CollectionUtils.newHashMap();
			metawidget.putClientProperty( getClass(), stateMap );
		}

		@SuppressWarnings( "unchecked" )
		State<UIComponent> state = stateMap.get( container );

		if ( state == null ) {
			state = new State<UIComponent>();
			stateMap.put( container, state );
		}

		return state;
	}

	@Override
	protected boolean isEmptyStub( UIComponent component ) {

		return ( component instanceof UIStub && component.getChildren().isEmpty() );
	}

	/**
	 * Overridden, and made final, to support searching for existing section widgets before creating
	 * new ones.
	 * <p>
	 * This avoids creating duplicate section widgets for components that use
	 * <code>UIMetawidget.COMPONENT_ATTRIBUTE_NOT_RECREATABLE</code>. Such components, and their
	 * enclosing section widgets, will survive POST-back so we must reconnect with them.
	 */

	@Override
	protected final UIComponent createSectionWidget( UIComponent previousSectionWidget, Map<String, String> attributes, UIComponent container, UIMetawidget metawidget ) {

		// If there is an existing section widget...

		String currentSection = getState( container, metawidget ).currentSection;

		for ( UIComponent child : container.getChildren() ) {

			Map<String, Object> childAttributes = child.getAttributes();
			@SuppressWarnings( "unchecked" )
			List<String> sectionDecorator = (List<String>) childAttributes.get( UIMetawidget.COMPONENT_ATTRIBUTE_SECTION_DECORATOR );

			if ( sectionDecorator == null ) {
				continue;
			}

			if ( !sectionDecorator.contains( currentSection ) ) {
				continue;
			}

			// ...re-lay it out (so that it appears properly positioned)...

			@SuppressWarnings( "unchecked" )
			Map<String, String> childMetadataAttributes = (Map<String, String>) childAttributes.get( UIMetawidget.COMPONENT_ATTRIBUTE_METADATA );
			getDelegate().layoutWidget( child, PROPERTY, childMetadataAttributes, container, metawidget );

			// ...and return its nested section widget (i.e. a layout Metawidget)

			UIComponent childToReturn = child;

			outer: while ( childToReturn != null ) {

				List<UIComponent> nestedChildren = childToReturn.getChildren();
				childToReturn = null;

				for ( UIComponent nestedChild : nestedChildren ) {

					if ( nestedChild.getAttributes().containsKey( COMPONENT_ATTRIBUTE_SECTION_WIDGET_TO_RETURN ) ) {
						return nestedChild;
					}

					@SuppressWarnings( "unchecked" )
					List<String> nestedChildSectionDecorator = (List<String>) nestedChild.getAttributes().get( UIMetawidget.COMPONENT_ATTRIBUTE_SECTION_DECORATOR );

					if ( nestedChildSectionDecorator.contains( currentSection ) ) {
						childToReturn = nestedChild;
						continue outer;
					}
				}
			}

			// Fall through
		}

		// Otherwise, create a new section widget...

		UIComponent sectionWidget = createNewSectionWidget( previousSectionWidget, attributes, container, metawidget );

		// ...and tag it (for layout Metawidget -> UITab -> UITabPanel, may need tagging at 3
		// levels)

		UIComponent taggedSectionWidget = sectionWidget;
		taggedSectionWidget.getAttributes().put( COMPONENT_ATTRIBUTE_SECTION_WIDGET_TO_RETURN, TRUE );

		while ( !taggedSectionWidget.equals( container ) ) {

			Map<String, Object> sectionWidgetParentAttributes = taggedSectionWidget.getAttributes();
			@SuppressWarnings( "unchecked" )
			List<String> sectionDecorator = (List<String>) sectionWidgetParentAttributes.get( UIMetawidget.COMPONENT_ATTRIBUTE_SECTION_DECORATOR );

			if ( sectionDecorator == null ) {
				sectionDecorator = CollectionUtils.newArrayList();
				sectionWidgetParentAttributes.put( UIMetawidget.COMPONENT_ATTRIBUTE_SECTION_DECORATOR, sectionDecorator );
			}

			sectionDecorator.add( currentSection );
			taggedSectionWidget = taggedSectionWidget.getParent();
		}

		return sectionWidget;
	}

	/**
	 * Creates a new widget to hold this section (<code>getState().currentSection</code>).
	 * <p>
	 * Clients should use this version of the method instead of <code>createSectionWidget</code>, in
	 * order to support <code>UIMetawidget.COMPONENT_ATTRIBUTE_NOT_RECREATABLE</code>.
	 *
	 * @param previousSectionWidget
	 *            the previous section widget (if any). This can be useful for tracing back to, say,
	 *            a TabHost
	 */

	protected abstract UIComponent createNewSectionWidget( UIComponent previousSectionWidget, Map<String, String> attributes, UIComponent container, UIMetawidget metawidget );
}

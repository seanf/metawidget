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

'use strict';

/**
 * WidgetProcessors.
 */

var metawidget = metawidget || {};
metawidget.widgetprocessor = metawidget.widgetprocessor || {};

//
// IdProcessor
//

metawidget.widgetprocessor.IdProcessor = function() {

	if ( ! ( this instanceof metawidget.widgetprocessor.IdProcessor ) ) {
		throw new Error( "Constructor called as a function" );
	}
};

metawidget.widgetprocessor.IdProcessor.prototype.processWidget = function( widget, attributes, mw ) {

	var id = attributes.name;
	
	if ( mw.path ) {
		var splitPath = mw.path.split( '.' );
		
		if ( attributes.name != '$root' ) {
			splitPath.push( attributes.name );
		}
		
		id = metawidget.util.camelCase( splitPath );
	}
		
	widget.setAttribute( 'id', id );	
	return widget;
};

//
// RequiredAttributeProcessor
//

metawidget.widgetprocessor.RequiredAttributeProcessor = function() {

	if ( ! ( this instanceof metawidget.widgetprocessor.RequiredAttributeProcessor ) ) {
		throw new Error( "Constructor called as a function" );
	}
};

metawidget.widgetprocessor.RequiredAttributeProcessor.prototype.processWidget = function( widget, attributes, mw ) {

	if ( attributes.required == 'true' ) {
		widget.setAttribute( 'required', 'required' );
	}
	
	return widget;
};

//
// ValueWidgetProcessor
//

/**
 * Simple data/action binding implementation. Frameworks that supply their own
 * data-binding mechanisms should override this with their own WidgetProcessor.
 */

metawidget.widgetprocessor.SimpleBindingProcessor = function() {

	if ( ! ( this instanceof metawidget.widgetprocessor.SimpleBindingProcessor ) ) {
		throw new Error( "Constructor called as a function" );
	}
};

metawidget.widgetprocessor.SimpleBindingProcessor.prototype.onStartBuild = function( mw ) {

	mw._simpleBindingProcessorBindings = {};
};

metawidget.widgetprocessor.SimpleBindingProcessor.prototype.processWidget = function( widget, attributes, mw ) {

	if ( widget.tagName == 'BUTTON' ) {
		var binding = mw.path;

		if ( attributes.name != '$root' ) {
			binding += '.' + attributes.name;
		}

		widget.setAttribute( 'onClick', 'return ' + binding + '()' );
	} else {

		var value;
		
		if ( attributes.name != '$root' && mw.toInspect ) {
			value = mw.toInspect[attributes.name];
		} else {
			value = mw.toInspect;
		}

		var isBindable = ( widget.tagName == 'INPUT' || widget.tagName == 'SELECT' || widget.tagName == 'TEXTAREA' );

		if ( isBindable && widget.getAttribute( 'id' )) {
			
			// Standard HTML works off 'name', not 'id', for binding
			
			widget.setAttribute( 'name', widget.getAttribute( 'id' ));
		}

		if ( value ) {
			if ( widget.tagName == 'OUTPUT' || widget.tagName == 'TEXTAREA' ) {
				widget.innerHTML = value;
			} else if ( widget.tagName == 'INPUT' && widget.getAttribute( 'type' ) == 'checkbox' ) {
				if ( value == true ) {
					widget.setAttribute( 'checked', 'checked' );
				}
			} else if ( isBindable ) {
				widget.setAttribute( 'value', value );
			}
		}

		if ( isBindable || widget.metawidget ) {
			mw._simpleBindingProcessorBindings[attributes.name] = widget;
		}
	}

	return widget;
};

/**
 * Save the bindings associated with the given Metawidget.
 */

metawidget.widgetprocessor.SimpleBindingProcessor.prototype.save = function( mw ) {

	for ( var name in mw._simpleBindingProcessorBindings ) {

		var widget = mw._simpleBindingProcessorBindings[name];
		
		if ( widget.metawidget ) {
			this.save( widget.metawidget );
			continue;
		}

		widget = document.getElementById( widget.id );
		
		if ( widget.getAttribute( 'type' ) == 'checkbox' ) {
			mw.toInspect[name] = ( widget.checked );
		} else {
			mw.toInspect[name] = widget.value;
		}
	}
};
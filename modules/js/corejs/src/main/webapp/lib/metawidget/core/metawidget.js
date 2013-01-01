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
 * Metawidget for pure JavaScript environments.
 */

var metawidget = metawidget || {};

metawidget.Metawidget = function( element, config ) {

	if ( ! ( this instanceof metawidget.Metawidget ) ) {
		throw new Error( "Constructor called as a function" );
	}

	var pipeline = new metawidget.Pipeline( element );
	pipeline.buildNestedMetawidget = function( attributes, mw ) {

		var nestedWidget = document.createElement( 'div' );

		// Duck-type our 'pipeline' as the 'config' of the nested Metawidget

		var nestedMetawidget = new metawidget.Metawidget( nestedWidget, pipeline );

		if ( mw.toInspect ) {
			nestedMetawidget.toInspect = mw.toInspect[attributes.name];
		}

		if ( mw.path ) {
			nestedMetawidget.path = mw.path + '.' + attributes.name;
		} else {
			// TODO: temporary safeguard against infinite recursion
			nestedMetawidget.path = attributes.name;
		}

		nestedMetawidget.readOnly = mw.readOnly || attributes.readOnly == 'true';

		// Attach ourselves as a property of the tag, rather than try to
		// 'extend' the built-in HTML tags

		nestedWidget.metawidget = nestedMetawidget;
		nestedMetawidget.buildWidgets();

		return nestedWidget;
	};

	// Configure defaults

	pipeline.inspector = new metawidget.inspector.PropertyTypeInspector();
	pipeline.widgetBuilder = new metawidget.widgetbuilder.CompositeWidgetBuilder( [ new metawidget.widgetbuilder.OverriddenWidgetBuilder(), new metawidget.widgetbuilder.ReadOnlyWidgetBuilder(),
			new metawidget.widgetbuilder.HtmlWidgetBuilder() ] );
	pipeline.widgetProcessors = [ new metawidget.widgetprocessor.IdProcessor(), new metawidget.widgetprocessor.RequiredAttributeProcessor(), new metawidget.widgetprocessor.SimpleBindingProcessor() ];
	pipeline.layout = new metawidget.layout.HeadingTagLayoutDecorator( new metawidget.layout.TableLayout() );
	pipeline.configure( config );

	// First time in, capture the contents of the Metawidget (if any)

	this._overriddenNodes = [];

	for ( var loop = 0, length = element.childNodes.length; loop < length; loop++ ) {
		this._overriddenNodes.push( element.childNodes[loop] );
	}

	//
	// Public methods
	//

	this.getWidgetProcessor = function( testInstanceOf ) {

		return pipeline.getWidgetProcessor( testInstanceOf );
	};

	this.setLayout = function( layout ) {

		pipeline.layout = layout;
	};

	this.buildWidgets = function() {

		// Defensive copy
		// TODO: test Defensive copy

		this.overriddenNodes = [];

		for ( var loop = 0, length = this._overriddenNodes.length; loop < length; loop++ ) {
			this.overriddenNodes.push( this._overriddenNodes[loop].cloneNode( true ) );
		}

		pipeline.buildWidgets( this );
	};
};

/**
 * Pipeline.
 * <p>
 * Clients should override 'buildNestedMetawidget'.
 */

metawidget.Pipeline = function( element ) {

	if ( ! ( this instanceof metawidget.Pipeline ) ) {
		throw new Error( "Constructor called as a function" );
	}

	this.inspectionResultProcessors = [];
	this.widgetProcessors = [];
	this.element = element;
};

/**
 * Configures the pipeline using the given config object.
 * <p>
 * This method is separate to the constructor, so that subclasses can set
 * defaults.
 */

metawidget.Pipeline.prototype.configure = function( config ) {

	if ( !config ) {
		return;
	}
	if ( config.inspector ) {
		this.inspector = config.inspector;
	}
	if ( config.inspectionResultProcessors ) {
		this.inspectionResultProcessors = config.inspectionResultProcessors.slice();
	}
	if ( config.widgetBuilder ) {
		this.widgetBuilder = config.widgetBuilder;
	}
	if ( config.widgetProcessors ) {
		this.widgetProcessors = config.widgetProcessors.slice();
	}
	if ( config.layout ) {
		this.layout = config.layout;
	}
};

/**
 * Searches the pipeline's current list of WidgetProcessors and matches each
 * against the given function
 * 
 * @param testInstanceOf
 *            a function that accepts a WidgetProcessor and will perform an
 *            'instanceof' test on it
 */

metawidget.Pipeline.prototype.getWidgetProcessor = function( testInstanceOf ) {

	for ( var loop = 0, length = this.widgetProcessors.length; loop < length; loop++ ) {

		var widgetProcessor = this.widgetProcessors[loop];

		if ( testInstanceOf( widgetProcessor ) ) {
			return widgetProcessor;
		}
	}
};

/**
 * Standard Metawidget pipeline.
 * 
 * @param mw
 *            Metawidget instance that will be passed down the pipeline
 *            (WidgetBuilders, WidgetProcessors etc). Expected to have
 *            'toInspect', 'path' and 'readOnly'.
 */

metawidget.Pipeline.prototype.buildWidgets = function( mw ) {

	// Inspector

	var inspectionResult;

	if ( this.inspector.inspect ) {
		inspectionResult = this.inspector.inspect( mw.toInspect, mw.path );
	} else {
		inspectionResult = this.inspector( mw.toInspect, mw.path );
	}

	// Inspector may return null

	if ( !inspectionResult ) {
		return;
	}

	// InspectionResultProcessors

	for ( var loop = 0, length = this.inspectionResultProcessors.length; loop < length; loop++ ) {

		var inspectionResultProcessor = this.inspectionResultProcessors[loop];

		if ( inspectionResultProcessor.processInspectionResult ) {
			inspectionResultProcessor.processInspectionResult( inspectionResult, mw );
		} else {
			inspectionResultProcessor( inspectionResult, mw );
		}
	}

	// Clear existing contents

	this.element.innerHTML = '';

	// onStartBuild

	if ( this.widgetBuilder.onStartBuild ) {
		this.widgetBuilder.onStartBuild( mw );
	}

	for ( var loop = 0, length = this.widgetProcessors.length; loop < length; loop++ ) {

		var widgetProcessor = this.widgetProcessors[loop];

		if ( widgetProcessor.onStartBuild ) {
			widgetProcessor.onStartBuild( mw );
		}
	}

	if ( this.layout.onStartBuild ) {
		this.layout.onStartBuild( mw );
	}

	if ( this.layout.startContainerLayout ) {
		this.layout.startContainerLayout( this.element, mw );
	}

	// Build top-level widget...

	for ( var loop = 0, length = inspectionResult.length; loop < length; loop++ ) {

		var attributes = inspectionResult[loop];

		if ( attributes.name != '$root' ) {
			continue;
		}

		var widget = _buildWidget( this, attributes, mw );

		if ( widget ) {
			widget = _processWidget( this, widget, attributes, mw );

			if ( widget ) {
				_layoutWidget( this, widget, attributes, this.element, mw );
				return;
			}
		}

		break;
	}

	// ...or try compound widget

	for ( var loop = 0, length = inspectionResult.length; loop < length; loop++ ) {

		var attributes = inspectionResult[loop];

		if ( attributes.name == '$root' ) {
			continue;
		}

		var widget = _buildWidget( this, attributes, mw );

		if ( !widget ) {
			widget = this.buildNestedMetawidget( attributes, mw );

			if ( !widget ) {
				return;
			}
		}

		widget = _processWidget( this, widget, attributes, mw );

		if ( widget ) {
			_layoutWidget( this, widget, attributes, this.element, mw );
		}
	}

	return;

	//
	// Private methods
	//

	function _buildWidget( pipeline, attributes, mw ) {

		if ( pipeline.widgetBuilder.buildWidget ) {
			return pipeline.widgetBuilder.buildWidget( attributes, mw );
		}

		return pipeline.widgetBuilder( attributes, mw );
	}

	function _processWidget( pipeline, widget, attributes, mw ) {

		for ( var loop = 0, length = pipeline.widgetProcessors.length; loop < length; loop++ ) {

			var widgetProcessor = pipeline.widgetProcessors[loop];

			if ( widgetProcessor.processWidget ) {
				widget = widgetProcessor.processWidget( widget, attributes, mw );
			} else {
				widget = widgetProcessor( widget, attributes, mw );
			}

			if ( !widget ) {
				return;
			}
		}

		return widget;
	}

	function _layoutWidget( pipeline, widget, attributes, container, mw ) {

		if ( pipeline.layout.layoutWidget ) {
			pipeline.layout.layoutWidget( widget, attributes, container, mw );
			return;
		}

		pipeline.layout( widget, attributes, container, mw );
	}
};

/**
 * Subclasses should override this method to create a nested Metawidget, using
 * their preferred widget creation methodology.
 */

metawidget.Pipeline.prototype.buildNestedMetawidget = function( attributes, mw ) {

	return null;
};

/*******************************************************************************************************
 *
 * GamaToolbar2.java, in ummisco.gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.3).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package ummisco.gama.ui.views.toolbar;

import static msi.gama.application.workbench.ThemeHelper.isDark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolItem;

import msi.gama.application.workbench.ThemeHelper;
import msi.gama.runtime.PlatformHelper;
import ummisco.gama.dev.utils.DEBUG;
import ummisco.gama.ui.controls.FlatButton;
import ummisco.gama.ui.resources.GamaColors;
import ummisco.gama.ui.resources.GamaColors.GamaUIColor;
import ummisco.gama.ui.resources.GamaIcon;
import ummisco.gama.ui.resources.IGamaColors;
import ummisco.gama.ui.views.toolbar.GamaToolbarFactory.ToggleAction;

/**
 * Class GamaToolbar. A declarative wrapper around 2 toolbars (left, right).
 *
 * @author drogoul
 * @since 3 déc. 2014
 *
 */
public class GamaToolbar2 extends Composite {

	{
		DEBUG.ON();
	}

	/** The right. */
	private GamaToolbarSimple left, right;

	/** The has tooltip. */
	private boolean hasTooltip;

	/** The height. */
	int height;

	/** The is visible. */
	boolean isVisible = true;

	/**
	 * Instantiates a new gama toolbar 2.
	 *
	 * @param parent
	 *            the parent
	 * @param style
	 *            the style
	 * @param height
	 *            the height
	 */
	public GamaToolbar2(final Composite parent, final int style, final int height) {
		super(parent, SWT.NONE);
		this.height = height;
		createLayout();
		createToolbars();
	}

	/**
	 * Empty to prevent the CSS engine from changing the color now and then (apparently randomly)
	 */
	@Override
	public void setBackground(final Color c) {
		// DEBUG.OUT("setBackground() Called by " + DEBUG.CALLER() + "." + DEBUG.METHOD());
	}

	/**
	 * Sets the background color.
	 *
	 * @param c
	 *            the new background color
	 */
	// Necessary to have the background color "stick"
	public void setBackgroundColor(final Color c) {
		// DEBUG.OUT("setBackgroundColor() called by = " + DEBUG.METHOD() + " of " + DEBUG.CALLER());
		// Calls super explicitly
		Color color = c;
		if (color == null) { color = isDark() ? getShell().getBackground() : IGamaColors.WHITE.color(); }
		super.setBackground(color);
		GamaColors.setBackground(color, this, left, right);
	}

	@Override
	public void setVisible(final boolean visible) { isVisible = visible; }

	@Override
	public boolean isVisible() { return isVisible; }

	/**
	 * Creates the layout.
	 */
	public void createLayout() {
		final var layout = new GridLayout(2, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginWidth = 5;
		layout.marginHeight = 0;
		setLayout(layout);
	}

	/**
	 * Creates the toolbars.
	 */
	public void createToolbars() {
		left = new GamaToolbarSimple(this, SWT.FLAT | SWT.HORIZONTAL | SWT.WRAP | SWT.NO_FOCUS | SWT.INHERIT_FORCE);
		var data = new GridData(SWT.LEFT, SWT.CENTER, true, true);
		data.minimumWidth = height * 2;
		left.setLayoutData(data);
		right = new GamaToolbarSimple(this, SWT.FLAT | SWT.HORIZONTAL | SWT.NO_FOCUS);
		data = new GridData(SWT.RIGHT, SWT.FILL, true, false);
		data.minimumWidth = height * 2;
		right.setLayoutData(data);
		setBackgroundColor(null);
	}

	/**
	 * No right toolbar.
	 */
	public void noLeftToolbar() {
		GridData data = (GridData) left.getLayoutData();
		data.minimumWidth = 0;
		data.grabExcessHorizontalSpace = false;
		data.widthHint = 0;
		data = (GridData) right.getLayoutData();
		data.grabExcessHorizontalSpace = true;
	}

	@Override
	protected void checkSubclass() {}

	/**
	 * Sep. Width is not used anymore
	 *
	 * @param width
	 *            the n
	 * @param side
	 *            the side
	 * @return the tool item
	 */
	public ToolItem sep(final int width, final int side /* SWT.LEFT or SWT.RIGHT */) {
		return sep(side);
	}

	/**
	 * Sep.
	 *
	 * @param side
	 *            the side
	 * @return the tool item
	 */
	public ToolItem sep(final int side /* SWT.LEFT or SWT.RIGHT */) {
		return new ToolItem(side == SWT.LEFT ? left : right, SWT.SEPARATOR);
	}

	/**
	 * Status.
	 *
	 * @param image
	 *            the image
	 * @param s
	 *            the s
	 * @param color
	 *            the color
	 * @param side
	 *            the side
	 * @return the tool item
	 */
	public ToolItem status(final Image image, final String s, final GamaUIColor color,
			final int side /* SWT.LEFT or SWT.RIGHT */) {
		wipe(side, true);
		final var item = button(color, s, image, side);
		getToolbar(side).requestLayout();
		return item;
	}

	/**
	 * Status.
	 *
	 * @param image
	 *            the image
	 * @param s
	 *            the s
	 * @param l
	 *            the l
	 * @param color
	 *            the color
	 * @param side
	 *            the side
	 * @return the tool item
	 */
	public ToolItem status(final Image image, final String s, final Selector l, final GamaUIColor color,
			final int side /* SWT.LEFT or SWT.RIGHT */) {
		wipe(side, true);
		final var item = button(color, s, image, side);
		((FlatButton) item.getControl()).addSelectionListener(l);
		requestLayout();
		return item;
	}

	/**
	 * Tooltip.
	 *
	 * @param s
	 *            the s
	 * @param rgb
	 *            the rgb
	 * @param side
	 *            the side
	 * @return the tool item
	 */
	public ToolItem tooltip(final String s, final GamaUIColor rgb, final int side /* SWT.LEFT or SWT.RIGHT */) {
		Color color = rgb == null ? getBackground() : rgb.color();
		if (s == null) return null;
		hasTooltip = true;
		final var tb = getToolbar(side);
		wipe(side, false);
		final var other = tb == right ? left : right;
		final var mySize = getSize().x;
		final var remainingLeftSize = tb.getSize().x;
		final var rightSize = other.getSize().x;

		final var width = mySize - remainingLeftSize - rightSize - 50;
		// wipe(side, false);
		tb.setLayout(new GridLayout(1, false));
		final var label = new Label(tb, SWT.WRAP | SWT.LEFT);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		// data.verticalIndent = 0;
		label.setLayoutData(data);
		label.setForeground(GamaColors.getTextColorForBackground(color).color());
		StringBuilder newString = new StringBuilder();
		// java.util.List<String> result = new ArrayList<>();
		try {
			final var reader = new BufferedReader(new StringReader(s));
			var line = reader.readLine();
			while (line != null) {
				if (!line.trim().isEmpty()) { newString.append(line).append(System.lineSeparator()); }
				line = reader.readLine();
			}
		} catch (final IOException exc) {}
		label.setText(newString.toString());
		// label.setFont(GamaFonts.getSmallFont());
		label.setBackground(color/* .inactive() */);
		final var t = control(label, /* c.computeSize(SWT.DEFAULT, SWT.DEFAULT).x + 10 */width, side);
		requestLayout();
		return t;
	}

	/**
	 * Check.
	 *
	 * @param image
	 *            the image
	 * @param text
	 *            the text
	 * @param tip
	 *            the tip
	 * @param listener
	 *            the listener
	 * @param side
	 *            the side
	 * @return the tool item
	 */
	public ToolItem check(final String image, final String text, final String tip, final Selector listener,
			final int side /* SWT.LEFT or SWT.RIGHT */) {
		return create(image, text, tip, listener, SWT.CHECK, false, null, side);
	}

	/**
	 * Check.
	 *
	 * @param command
	 *            the command
	 * @param side
	 *            the side
	 * @return the tool item
	 */
	public ToolItem check(final GamaCommand command, final int side) {
		return check(command.getImage(), command.getText(), command.getTooltip(), command.getListener(), side);
	}

	/**
	 * Button.
	 *
	 * @param image
	 *            the image
	 * @param text
	 *            the text
	 * @param tip
	 *            the tip
	 * @param listener
	 *            the listener
	 * @param side
	 *            the side
	 * @return the tool item
	 */
	public ToolItem button(final String image, final String text, final String tip, final Selector listener,
			final int side /* SWT.LEFT or SWT.RIGHT */) {
		return create(image, text, tip, listener, SWT.PUSH, false, null, side);
	}

	/**
	 * Button.
	 *
	 * @param command
	 *            the command
	 * @param side
	 *            the side
	 * @return the tool item
	 */
	public ToolItem button(final GamaCommand command, final int side) {
		return button(command.getImage(), command.getText(), command.getTooltip(), command.getListener(), side);
	}

	/**
	 * Button.
	 *
	 * @param color
	 *            the color
	 * @param text
	 *            the text
	 * @param listener
	 *            the listener
	 * @param side
	 *            the side
	 * @return the tool item
	 */
	public ToolItem button(final GamaUIColor color, final String text, final Selector listener, final int side) {
		final var button = FlatButton.button(side == SWT.LEFT ? left : right, color, text, null);
		button.addSelectionListener(listener);
		return control(button, button.computeSize(SWT.DEFAULT, button.getHeight(), false).x + 4, side);
	}

	/**
	 * Button.
	 *
	 * @param color
	 *            the color
	 * @param text
	 *            the text
	 * @param image
	 *            the image
	 * @param side
	 *            the side
	 * @return the tool item
	 */
	public ToolItem button(final GamaUIColor color, final String text, final Image image, final int side) {
		final var button = FlatButton.button(side == SWT.LEFT ? left : right, color, text, image);
		return control(button, button.computeSize(SWT.DEFAULT, button.getHeight(), false).x + 4, side);
	}

	/**
	 * Button.
	 *
	 * @param color
	 *            the color
	 * @param text
	 *            the text
	 * @param image
	 *            the image
	 * @param listener
	 *            the listener
	 * @param side
	 *            the side
	 * @return the tool item
	 */
	public ToolItem button(final GamaUIColor color, final String text, final Image image, final Selector listener,
			final int side) {
		final var button = FlatButton.button(side == SWT.LEFT ? left : right, color, text, image);
		button.addSelectionListener(listener);
		return control(button, button.computeSize(SWT.DEFAULT, button.getHeight(), false).x + 4, side);
	}

	/**
	 * Menu.
	 *
	 * @param color
	 *            the color
	 * @param text
	 *            the text
	 * @param side
	 *            the side
	 * @return the tool item
	 */
	public ToolItem menu(final GamaUIColor color, final String text, final int side) {
		final var button = FlatButton.menu(side == SWT.LEFT ? left : right, color, text);
		return control(button, button.computeSize(SWT.DEFAULT, button.getHeight(), false).x + 4, side);
	}

	/**
	 * Menu.
	 *
	 * @param image
	 *            the image
	 * @param text
	 *            the text
	 * @param tip
	 *            the tip
	 * @param listener
	 *            the listener
	 * @param side
	 *            the side
	 * @return the tool item
	 */
	public ToolItem menu(final String image, final String text, final String tip, final Selector listener,
			final int side /* SWT.LEFT or SWT.RIGHT */) {
		return create(image, text, tip, listener, SWT.DROP_DOWN, false, null, side);
	}

	/**
	 * Control.
	 *
	 * @param c
	 *            the c
	 * @param width
	 *            the width
	 * @param side
	 *            the side
	 * @return the tool item
	 */
	public ToolItem control(final Control c, final int width, final int side /* SWT.LEFT or SWT.RIGHT */) {
		final var control = create(null, null, null, null, SWT.SEPARATOR, false, c, side);
		if (width == SWT.DEFAULT) {
			control.setWidth(c.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
		} else {
			control.setWidth(width);
		}
		return control;
	}

	/**
	 * Refresh.
	 *
	 * @param layout
	 *            the layout
	 */
	@Override
	public void requestLayout() {
		left.requestLayout();
		right.requestLayout();
		super.requestLayout();
		getParent().requestLayout();
	}

	/**
	 * Visually update.
	 */
	// public void visuallyUpdate() {
	// GamaToolbarFactory.visuallyUpdate(left);
	// GamaToolbarFactory.visuallyUpdate(right);
	// }

	/**
	 * Wipes the toolbar (left or right), including or not the simple tool items. Retuns the width of the toolbar once
	 * wiped.
	 *
	 * @param side
	 * @param includingToolItems
	 * @return
	 */
	public void wipe(final int side /* SWT.LEFT or SWT.RIGHT */, final boolean includingToolItems) {
		final var items = getToolbar(side).getItems();
		for (final ToolItem t : items) {
			final var c = t.getControl();
			if (c == null && includingToolItems || c != null) {
				if (c != null) { c.dispose(); }
				t.dispose();
			}
		}
		normalizeToolbars();
		requestLayout();

	}

	/**
	 * Item.
	 *
	 * @param item
	 *            the item
	 * @param side
	 *            the side
	 */
	public void item(final IContributionItem item, final int side) {
		item.fill(getToolbar(side), getToolbar(side).getItemCount());
	}

	/**
	 * Creates the.
	 *
	 * @param image
	 *            the image
	 * @param text
	 *            the text
	 * @param tip
	 *            the tip
	 * @param listener
	 *            the listener
	 * @param style
	 *            the style
	 * @param forceText
	 *            the force text
	 * @param control
	 *            the control
	 * @param side
	 *            the side
	 * @return the tool item
	 */
	private ToolItem create(final String image, final String text, final String tip, final SelectionListener listener,
			final int style, final boolean forceText, final Control control,
			final int side /* SWT.LEFT or SWT.RIGHT */) {
		final var tb = getToolbar(side);
		// DOES NOT SEEM TO BE NECESSARY ANYMORE (SEE #3677)
		// if ( tb.getItemCount() == 0 && PlatformHelper.isWindows()) {
		// int h = height;
		// if (control != null) { h = Math.max(h, control.computeSize(SWT.DEFAULT, SWT.DEFAULT).y) + 4; }
		// final var icon = GamaIcon.ofSize(getBackground(), 2, h);
		// final var button = new ToolItem(tb, SWT.NONE);
		// final var im = icon.image();
		// button.setImage(im);
		// button.setDisabledImage(im);
		// button.setEnabled(false);
		// }
		final var button = new ToolItem(tb, style);
		if (text != null && forceText) { button.setText(text); }
		if (tip != null) { button.setToolTipText(tip); }
		if (image != null) {
			button.setData(image);
			GamaIcon icon = GamaIcon.named(image);
			button.setImage(icon.image());
			button.setDisabledImage(icon.disabled());
		}
		if (listener != null) {
			if (style == SWT.CHECK) {
				button.addSelectionListener((Selector) e -> {
					checkSelectionIcon(button);
					listener.widgetSelected(e);
				});
			} else {
				button.addSelectionListener(listener);
			}
		}
		if (control != null) {
			// GamaColors.setBackground(control, getBackground());
			button.setControl(control);
			control.requestLayout();
		}
		normalizeToolbars();
		tb.requestLayout();
		return button;
	}

	/**
	 * Normalize toolbars.
	 */
	private void normalizeToolbars() {
		// final int n = right.getItemCount();
		var size = 0;
		for (final ToolItem t : right.getItems()) { size += t.getWidth(); }
		((GridData) right.getLayoutData()).minimumWidth = size;
	}

	/**
	 * @param right2
	 * @return
	 */
	public GamaToolbarSimple getToolbar(final int side) {
		return side == SWT.LEFT ? left : right;
	}

	/**
	 * @return
	 */
	public boolean hasTooltip() {
		return hasTooltip;
	}

	/** The toggle. */
	private ToggleAction toggle;

	/**
	 * Sets the toogle action.
	 *
	 * @param toggle
	 *            the new toogle action
	 */
	public void setToogleAction(final ToggleAction toggle) { this.toggle = toggle; }

	/**
	 * Hide.
	 */
	public void hide() {
		isVisible = true; // force to true
		toggle.run(); // will make it false
	}

	/**
	 * Show.
	 */
	public void show() {
		isVisible = false; // force to false
		toggle.run(); // will make it true
	}

	/**
	 * Sets the default height.
	 *
	 * @param height
	 *            the new default height
	 */
	public void setDefaultHeight(final int height) {
		this.height = height;

	}

	/**
	 * Check selection icon.
	 *
	 * @param button
	 *            the button
	 * @param tb
	 *            the tb
	 */
	private void checkSelectionIcon(final ToolItem button) {
		String image = (String) button.getData();
		if (image == null) return;
		if (PlatformHelper.isMac() && GamaColors.isDark(getBackground()) && !ThemeHelper.isDark()) {
			if (button.getSelection()) {
				button.setImage(GamaIcon.named(image).checked());
			} else {
				button.setImage(GamaIcon.named(image).image());
			}
		}
	}

	/**
	 * Sets the selection.
	 *
	 * @param item
	 *            the item
	 * @param selected
	 *            the selected
	 */
	public void setSelection(final ToolItem item, final boolean selected) {
		item.setSelection(selected);
		checkSelectionIcon(item);
	}

}

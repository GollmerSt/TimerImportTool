/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2017-06-20 23:11:39 +0200 (Di, 20 Jun 2017) $
 *   $Author: ds10 $
 * $Revision: 8772 $
 */

package devplugin;

import javax.swing.Icon;

/**
 * Objects which wants to mark programs must implement this interface.
*/
public interface Marker {

  /**
   * @return The id of this Marker.
   */
  public String getId();
  
  /**
   * @return The default mark icon for this Marker
   */
  public Icon getMarkIcon();
  
  /** 
   * @param p The program to check.
   * @return An array with icon to use for marking of the given program
   * or <code>null</code> if this Marker doesn't sets icons for the program.
   * @since 2.5 
   */
  public Icon[] getMarkIcons(Program p);
  
  /** 
   * Gets the mark priority that this Marker uses for the given program.
   * <p>
   * The mark priority can be
   * <ul>
   * <li>{@link Program#PRIORITY_MARK_NONE},</li>
   * <li>{@link Program#PRIORITY_MARK_MIN},</li>
   * <li>{@link Program#PRIORITY_MARK_MEDIUM_LOWER},</li>
   * <li>{@link Program#PRIORITY_MARK_MEDIUM},</li>
   * <li>{@link Program#PRIORITY_MARK_MEDIUM_HIGHER} or</li>
   * <li>{@link Program#PRIORITY_MARK_MAX}.</li>
   * </ul>
   * <p>
   *
   * @param p The program to check.
   * @return The mark priority for the given program.
   * @since 2.5.1 
   */
  public int getMarkPriorityForProgram(Program p);

}

package org.w3c.dom.html2;

/**
 * Unordered list. See the UL element definition in HTML 4.01.
 * <p>See also the <a href='http://www.w3.org/TR/2003/REC-DOM-Level-2-HTML-20030109'>Document Object Model (DOM) Level 2 HTML Specification</a>.
 */
public interface HTMLUListElement extends HTMLElement {
    /**
     * Reduce spacing between list items. See the compact attribute definition 
     * in HTML 4.01. This attribute is deprecated in HTML 4.01.
     */
    public boolean getCompact();
    /**
     * Reduce spacing between list items. See the compact attribute definition 
     * in HTML 4.01. This attribute is deprecated in HTML 4.01.
     */
    public void setCompact(boolean compact);

    /**
     * Bullet style. See the type attribute definition in HTML 4.01. This 
     * attribute is deprecated in HTML 4.01.
     */
    public String getType();
    /**
     * Bullet style. See the type attribute definition in HTML 4.01. This 
     * attribute is deprecated in HTML 4.01.
     */
    public void setType(String type);

}

<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet
  version="2.0"
  xmlns:c2="http://schemas.io7m.com/changelog/2.0.0"
  xmlns:c="urn:com.io7m.changelog:4.0"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output omit-xml-declaration="yes" indent="yes"/>

  <xsl:template match="c2:changelog">
    <xsl:element name="c:changelog">
      <xsl:attribute name="project">
        <xsl:value-of select="c2:project"/>
      </xsl:attribute>

      <xsl:element name="c:releases">
        <xsl:for-each select="c2:release">
          <xsl:element name="c:release">

            <xsl:attribute name="date">
              <xsl:variable name="t" select="concat(normalize-space(c2:date), 'T00:00:00+00:00')"/>
              <xsl:variable name="date" select="xs:dateTime($t)"/>
              <xsl:value-of select="format-dateTime($date, '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]+00:00')"/>
            </xsl:attribute>

            <xsl:attribute name="version">
              <xsl:value-of select="c2:version"/>
            </xsl:attribute>

            <xsl:attribute name="ticket-system">
              <xsl:value-of select="@c2:ticket-system"/>
            </xsl:attribute>

            <xsl:element name="c:changes">
              <xsl:for-each select="c2:item">
                <xsl:element name="c:change">

                  <xsl:attribute name="date">
                    <xsl:variable name="t" select="concat(normalize-space(c2:date), 'T00:00:00+00:00')"/>
                    <xsl:variable name="date" select="xs:dateTime($t)"/>
                    <xsl:value-of select="format-dateTime($date, '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]+00:00')"/>
                  </xsl:attribute>

                  <xsl:attribute name="summary">
                    <xsl:value-of select="normalize-space(c2:summary)"/>
                  </xsl:attribute>

                  <xsl:for-each select="c2:type-code-change">
                    <xsl:attribute name="compatible">false</xsl:attribute>
                  </xsl:for-each>

                  <xsl:if test="count(c2:ticket) > 0">
                    <xsl:element name="c:tickets">
                      <xsl:for-each select="c2:ticket">
                        <xsl:element name="c:ticket">
                          <xsl:attribute name="id">
                            <xsl:value-of select="normalize-space(.)"/>
                          </xsl:attribute>
                        </xsl:element>
                      </xsl:for-each>
                    </xsl:element>
                  </xsl:if>

                </xsl:element>
              </xsl:for-each>
            </xsl:element>
          </xsl:element>
        </xsl:for-each>
      </xsl:element>

      <xsl:element name="c:ticket-systems">
        <xsl:for-each select="c2:ticket-system">
          <xsl:element name="c:ticket-system">
            <xsl:attribute name="id"><xsl:value-of select="@xml:id"/></xsl:attribute>
            <xsl:attribute name="url"><xsl:value-of select="c2:ticket-url"/></xsl:attribute>
          </xsl:element>
        </xsl:for-each>
      </xsl:element>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>


package fiji.plugin.vollseg_kymo.util;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.JFreeChart;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * A collection of static utilities made to export a JPanel to various scalable
 * file format.
 * 
 * @author Jean-Yves Tinevez, 2011 - 2021
 */
public class ChartExporter
{

	/**
	 * Export a JFreeChart to SVG.
	 * 
	 * @param svgFile
	 *            the target svg file.
	 * @param chart
	 *            the chart to export.
	 * @param width
	 *            the width of the panel the chart is painted in.
	 * @param height
	 *            the height of the panel the chart is painted in.
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public static void exportChartAsSVG( final File svgFile, final JFreeChart chart, final int width, final int height ) throws UnsupportedEncodingException, IOException
	{
		// Get a DOMImplementation and create an XML document
		final org.w3c.dom.DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		final org.w3c.dom.Document document = domImpl.createDocument( null, "svg", null );

		// Create an instance of the SVG Generator
		final SVGGraphics2D svgGenerator = new SVGGraphics2D( document );
		// draw the chart in the SVG generator
		chart.draw( svgGenerator, new Rectangle( width, height ) );

		// Write svg file
		try (final OutputStream outputStream = new FileOutputStream( svgFile );
				final Writer out = new OutputStreamWriter( outputStream, "UTF-8" ))
		{
			svgGenerator.stream( out, true );
		}
	}

	/**
	 * Export a JFreeChart to PDF.
	 * 
	 * @param pdfFile
	 *            the target pdf file.
	 * @param chart
	 *            the chart to export.
	 * @param width
	 *            the width of the panel the chart is painted in.
	 * @param height
	 *            the height of the panel the chart is painted in.
	 * @throws FileNotFoundException
	 *             if the file to write cannot be found.
	 * @throws DocumentException
	 *             on error.
	 */
	public static void exportChartAsPDF( final File pdfFile, final JFreeChart chart, final int width, final int height ) throws FileNotFoundException, DocumentException
	{
		// step 1
		final com.itextpdf.text.Rectangle pageSize = new com.itextpdf.text.Rectangle( 0, 0, width, height );
		final com.itextpdf.text.Document document = new com.itextpdf.text.Document( pageSize );
		// step 2
		final PdfWriter writer = PdfWriter.getInstance( document, new FileOutputStream( pdfFile ) );
		// step 3
		document.open();
		// step 4
		final PdfContentByte cb = writer.getDirectContent();
		final PdfGraphics2D g2 = new PdfGraphics2D( cb, width, height );
		chart.draw( g2, new Rectangle( 0, 0, width, height ) );
		g2.dispose();
		// step 5
		document.close();
	}
}

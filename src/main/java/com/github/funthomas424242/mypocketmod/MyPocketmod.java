package com.github.funthomas424242.mypocketmod;

/*-
 * #%L
 * MyPocketmod
 * %%
 * Copyright (C) 2018 - 2019 PIUG
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;


public class MyPocketmod {

    final protected Configuration configuration;

    public MyPocketmod() {
        configuration = Configuration.createNewConfiguration().initialize();
    }

    protected byte[] getPDFPageAsBytes(final Path absoluteFilePath, final int timesQuadrantRotatePortrait, final Configuration.Orientation vorgabeOrientation) throws IOException {
        final PDDocument seite = loadPage(absoluteFilePath);
        final BufferedImage image = getPDFAsImage(seite);
        seite.close();

        // Bestimmung Hoch oder Querformat
        final int hoehe = image.getHeight();
        final int breite = image.getWidth();
        int timesQuadrantRotateLandscape = timesQuadrantRotatePortrait - 1;


        final int timesQuadrantRotate;
        switch (vorgabeOrientation) {
            case HOCHFORMAT: {
                timesQuadrantRotate = timesQuadrantRotatePortrait;
                break;
            }
            case QUERFORMAT: {
                timesQuadrantRotate = timesQuadrantRotateLandscape;
                break;
            }
            case AUTO: // default -> no break
            default: {
                timesQuadrantRotate = (hoehe > breite) ? timesQuadrantRotatePortrait : timesQuadrantRotateLandscape;
                break;
            }

        }


        if (timesQuadrantRotate > 0) {
            final BufferedImage rotatedImage = rotateImageByDegrees(image, (double) 90 * timesQuadrantRotate);
            return convertImage2Bytes(rotatedImage);
        } else {
            return convertImage2Bytes(image);
        }
    }

    /**
     * Quelle: https://stackoverflow.com/a/37758533/373498
     *
     * @param img   Zu drehendes Bild
     * @param angle Zu drehende Gradzahl im Uhrzeigersinn.
     * @return gedrehtes Bild
     */
    public BufferedImage rotateImageByDegrees(BufferedImage img, double angle) {

        final double rads = Math.toRadians(angle);
        final double sin = Math.abs(Math.sin(rads));
        final double cos = Math.abs(Math.cos(rads));
        int w = img.getWidth();
        int h = img.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);
        int x = w / 2;
        int y = h / 2;

        at.rotate(rads, x, y);
        AffineTransformOp rotationTransformOp = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return rotationTransformOp.filter(img, null);
    }

    protected byte[] convertImage2Bytes(final BufferedImage image) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        baos.flush();
        return baos.toByteArray();
    }

    private BufferedImage getPDFAsImage(PDDocument doc1) throws IOException {
        //Instantiating the PDFRenderer class
        final PDFRenderer renderer = new PDFRenderer(doc1);
        //Rendering an image from the PDF document
        return renderer.renderImage(0);
    }

    protected PDDocument loadPage(final Path absoluteFilePath) throws IOException {
        //Loading page x
        final File pageFile = absoluteFilePath.toFile();
        System.out.println(pageFile.getAbsolutePath());
        return PDDocument.load(pageFile);
    }
}




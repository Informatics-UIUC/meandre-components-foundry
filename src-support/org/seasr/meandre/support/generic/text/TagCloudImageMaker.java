/**
*
* University of Illinois/NCSA
* Open Source License
*
* Copyright (c) 2008, NCSA.  All rights reserved.
*
* Developed by:
* The Automated Learning Group
* University of Illinois at Urbana-Champaign
* http://www.seasr.org
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal with the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject
* to the following conditions:
*
* Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimers.
*
* Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimers in
* the documentation and/or other materials provided with the distribution.
*
* Neither the names of The Automated Learning Group, University of
* Illinois at Urbana-Champaign, nor the names of its contributors may
* be used to endorse or promote products derived from this Software
* without specific prior written permission.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
*
*/

package org.seasr.meandre.support.generic.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

/**
 * @author Lily Dong
 * @author Boris Capitanu
 */
public class TagCloudImageMaker {

    private int _canvasWidth, _canvasHeight;
    private float _maxFontSize, _minFontSize;
    private String _fontName;
    private boolean _showCounts;
    private Random _rand;
    private long _seed;

    public TagCloudImageMaker(long seed, int canvasWidth, int canvasHeight,
            String fontName, float minFontSize, float maxFontSize, boolean showCounts) {
        _canvasWidth = canvasWidth;
        _canvasHeight = canvasHeight;
        _fontName = fontName;
        _maxFontSize = maxFontSize;
        _minFontSize = minFontSize;
        _showCounts = showCounts;
    	_seed = seed;

    }

    public void setCanvasWidth(int canvasWidth) {
        if (canvasWidth <= 0)
            throw new IllegalArgumentException("" + canvasWidth);

        _canvasWidth = canvasWidth;
    }

    public int getCanvasWidth() {
        return _canvasWidth;
    }

    public void setCanvasHeight(int canvasHeight) {
        if (canvasHeight <= 0)
            throw new IllegalArgumentException("" + canvasHeight);

        _canvasHeight = canvasHeight;
    }

    public int getCanvasHeight() {
        return _canvasHeight;
    }

    public void setFontName(String fontName) {
        if (fontName == null || fontName.trim().length() == 0)
            throw new IllegalArgumentException(fontName);

        _fontName = fontName;
    }

    public String getFontName() {
        return _fontName;
    }

    public void setMaxFontSize(int maxFontSize) {
        if (maxFontSize <= 0)
            throw new IllegalArgumentException("" + maxFontSize);

        _maxFontSize = maxFontSize;
    }

    public float getMaxFontSize() {
        return _maxFontSize;
    }

    public void setMinFontSize(int minFontSize) {
        if (minFontSize <= 0)
            throw new IllegalArgumentException("" + minFontSize);

        _minFontSize = minFontSize;
    }

    public float getMinFontSize() {
        return _minFontSize;
    }

    public void setShowCounts(boolean value) {
        _showCounts = value;
    }

    public boolean getShowCounts() {
        return _showCounts;
    }

    public TagCloudImage createTagCloudImage(Map<String, Integer> wordCounts)
        throws InterruptedException {
    	
        if (wordCounts == null || wordCounts.size() == 0) return null;

        _rand = new Random(_seed);

        int length = wordCounts.size();
        String[] text = new String[length];
        int[] fontSize = new int[length];
        int[] count = new int[length];

        int pos = 0;
        for (Entry<String, Integer> entry : wordCounts.entrySet()) {
            text[pos] = entry.getKey();
            count[pos] = entry.getValue();
            fontSize[pos++] = entry.getValue();
        }

        for(int i=0; i<count.length-1; i++) { //sort
            int p = i; //p points to the biggest value
            for(int j=i+1; j<count.length; j++) {
                if(count[j] > count[p])
                    p = j;
            }
            if(p != i) { //swap
                String s = text[i];
                text[i] = text[p];
                text[p] = s;
                int t = count[i];
                count[i] = count[p];
                count[p] = t;
                t = fontSize[i];
                fontSize[i] = fontSize[p];
                fontSize[p] = t;
            }
        }
        int maxValue = fontSize[0],
            minValue = fontSize[fontSize.length-1];

        Color[] colors = {new Color(0x99, 0x33, 0x33),
                  new Color(0x99, 0x66, 0x33),
                  new Color(0x99, 0x99, 0x33),
                  new Color(0x99, 0xcc, 0x33),
                  new Color(0x99, 0xff, 0x33)};

        int margin = 5;

        if(maxValue != minValue) {
            float slope = (_maxFontSize - _minFontSize)/(maxValue-minValue);
            for(int k=0; k<fontSize.length; k++)
                fontSize[k] = (int)(_minFontSize+slope*(fontSize[k]-minValue));
        } else {
            for(int k=0; k<fontSize.length; k++)
                fontSize[k] = (int)_minFontSize;
        }

        boolean[][] grid = new boolean[_canvasWidth][_canvasHeight];
        for(int i=0; i<grid.length; i++)
            for(int j=0; j<grid[0].length; j++)
                grid[i][j] = false;

        TagCloudImage image = new TagCloudImage(_canvasWidth, _canvasHeight, BufferedImage.TYPE_INT_ARGB);
        image.setTotalWords(text.length);

        Graphics2D g2D = image.createGraphics();
        FontRenderContext frc = g2D.getFontRenderContext();

        boolean done = false;
        int k;
        for(k=0; k<text.length; k++) {
            String str = (_showCounts)? text[k]+" "+count[k]: text[k];

            Font font = new Font(_fontName, Font.BOLD, fontSize[k]);
            TextLayout layout = new TextLayout(/*text[k]*/str, font, frc);

            int w = (int)layout.getVisibleAdvance(),
                h = (int)(layout.getAscent()+layout.getDescent());

            int textWidth = w+2*margin,
                textHeight = h+2*margin;

            int xCoord = (textWidth-w)/2,
                yCoord = (int)layout.getAscent()+(textHeight-h)/2;

            BufferedImage textImage = new BufferedImage(
                    textWidth, textHeight,BufferedImage.TYPE_INT_ARGB);
            Graphics2D textG2D = textImage.createGraphics();

            textG2D.setColor(Color.white);
            textG2D.fillRect(0, 0, textWidth-1, textHeight-1);
            textG2D.setColor(colors[k%colors.length]);
            textG2D.setFont(font);
            textG2D.drawString(/*text[k]*/str, xCoord, yCoord);

            BufferedImage biFlip = null;
            if(k%5 == 0) {
                biFlip = new BufferedImage(textHeight, textWidth,textImage.getType());
                for(int i=0; i<textWidth; i++)
                    for(int j=0; j<textHeight; j++)
                        biFlip.setRGB(textHeight-1-j, i, textImage.getRGB(i, j));
                        //biFlip.setRGB(j, textWidth-1-i, textImage.getRGB(i, j));
                textImage = biFlip;
                int tmp = textWidth;
                textWidth = textHeight;
                textHeight = tmp;
            }

            int[] pixels = new int [textWidth * textHeight];
            PixelGrabber pg = new PixelGrabber (textImage, 0, 0, textWidth, textHeight,
                        pixels, 0, textWidth);
            pg.grabPixels ();

            boolean[][] mask = new boolean[textWidth][textHeight];
            for(int i=0; i<mask.length; ++i)
                for(int j=0; j<mask[0].length; ++j)
                    mask[i][j] = false;
            for (int j=0; j<textHeight; j+=margin) {
                for (int i=0; i< textWidth; i+=margin) {
                    boolean found = false;
                    for(int ii=i; ii<i+margin&&ii<textWidth-margin; ii++) {
                        for(int jj=j; jj<j+margin&&jj<textHeight-margin; jj++) {
                            int value = pixels[jj * textWidth + ii];
                            byte[] rgb = new byte[3];
                            rgb [0] = (byte) (value & 0xFF);
                            rgb [1] = (byte) ((value >> 8) & 0xFF);
                            rgb [2] = (byte) ((value >>  16) & 0xFF);
                            //if(rgb[0]!=0 || rgb[1]!=0 || rgb[2]!=0) {
                            if(!(rgb[0]==-1  && rgb[1]==-1 && rgb[2]==-1)) {
                                //textG2D.fillRect(i, j, 5, 5);
                                mask[i][j] = true;
                                found = true;
                                break;
                            }
                        }//jj
                        if(found)
                            break;
                    }//ii
                }//i
            }//j


            double a = _rand.nextDouble() * Math.PI;
            double d = _rand.nextDouble() * (Math.max(textWidth, textHeight)/4);
            double da = (_rand.nextDouble()-0.5) / 2;
            double dd = 0.05;
            int x, y;
            int nr = 0;

            while (true) {
                x = (int)(Math.floor((_canvasWidth/2 + (Math.cos(a)*d*2) - (textWidth/2))/5)*5);
                y = (int)(Math.floor((_canvasHeight/2 + (Math.sin(a)*d) - (textHeight/2))/5)*5);

                x = (x<0)?0: x;
                y = (y<0)?0: y;

                boolean fail = false;
                for (int xt=0; xt<textWidth && !fail; xt+=margin) {
                    for (int yt=0; yt<textHeight && !fail; yt+=margin) {
                        if(xt+x>=_canvasWidth ||
                           yt+y>=_canvasHeight ||
                           (mask[xt][yt] && grid[xt + x][yt + y]))
                                fail = true;
                    }
                }
                if (!fail)
                    break;
                a += da;
                d += dd;

                if(++nr>10000) {//endless loop
                    done = true; //finished ahead of schedule
                    break;
                }
            }
            if(!done) {
                for (int xt=0; xt<textWidth; xt+=margin) {
                    for (int yt = 0; yt<textHeight; yt+=margin)
                        if (mask[xt][yt])
                            grid[xt+x][yt+y] = true;
                }
                for(int i=0; i<textWidth; i++)
                    for(int j=0; j<textHeight; j++)
                        if(mask[(i/margin)*margin][(j/margin)*margin])
                            image.setRGB(x+i, y+j, textImage.getRGB(i, j));
            } else
                break;
        }//k

        image.setShownWords(k);

        //g.drawImage(image, 0, 0, this);
        return image;
    }
}

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

package org.seasr.meandre.support.generic.text.analytics;

import java.util.StringTokenizer;

import org.seasr.meandre.support.generic.text.SyntacticUtils;

/**
 * @author Boris Capitanu
 *
 */
public class ReadabilityMeasure {

    private final int totalSyllables;
    private final int totalWords;
    private final int totalSentences;
    private final float fleschReadingEaseScore;
    private final float fleschGradeLevel;

    public ReadabilityMeasure(int syllables, int words, int sentences, float fres, float fgl) {
        totalSyllables = syllables;
        totalWords = words;
        totalSentences = sentences;
        fleschReadingEaseScore = fres;
        fleschGradeLevel = fgl;
    }

    public int getSyllables() {
        return totalSyllables;
    }

    public int getWords() {
        return totalWords;
    }

    public int getSentences() {
        return totalSentences;
    }

    public float getReadingEaseScore() {
        return fleschReadingEaseScore;
    }

    public float getGradeLevel() {
        return fleschGradeLevel;
    }

    public static ReadabilityMeasure computeFleschReadabilityMeasure(String content) {
        int syllables = 0;
        int sentences = 0;
        int words     = 0;

        String delimiters = ".,':;?{}[]=-+_!@#$%^&*() ";
        StringTokenizer tokenizer = new StringTokenizer(content,delimiters);
        //go through all words
        while (tokenizer.hasMoreTokens())
        {
            String word = tokenizer.nextToken();
            syllables += SyntacticUtils.countSyllables(word);
            words++;
        }
        //look for sentence delimiters
        String sentenceDelim = ".:;?!";
        StringTokenizer sentenceTokenizer = new StringTokenizer(content,sentenceDelim);
        sentences = sentenceTokenizer.countTokens();

        //calculate flesch reading ease score
        final float f1 = (float) 206.835;
        final float f2 = (float) 84.6;
        final float f3 = (float) 1.015;
        float r1 = (float) syllables / (float) words;
        float r2 = (float) words / (float) sentences;
        float fres = f1 - (f2*r1) - (f3*r2);

        //calculate the flesch grade level
        float fgl = 0.39f * ((float) words / (float)sentences) +
        11.8f * ((float)syllables / (float)words) - 15.59f;

        return new ReadabilityMeasure(syllables, words, sentences, fres, fgl);
    }
}

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

/**
 * @author Boris Capitanu
 *
 */
public abstract class SyntacticUtils {
    // A method to count the number of syllables in a word
    // Pretty basic, just based off of the number of vowels
    // This could be improved
    public static int countSyllables(String word) {
        int      syl    = 0;
        boolean  vowel  = false;
        int      length = word.length();

        //check each word for vowels (don't count more than one vowel in a row)
        for(int i=0; i<length; i++) {
            if        (isVowel(word.charAt(i)) && (vowel==false)) {
                vowel = true;
                syl++;
            } else if (isVowel(word.charAt(i)) && (vowel==true)) {
                vowel = true;
            } else {
                vowel = false;
            }
        }

        char tempChar = word.charAt(word.length()-1);
        //check for 'e' at the end, as long as not a word w/ one syllable
        if (((tempChar == 'e') || (tempChar == 'E')) && (syl != 1)) {
            syl--;
        }
        return syl;
    }

    //check if a char is a vowel (count y)
    public static boolean isVowel(char c) {
        if      ((c == 'a') || (c == 'A')) { return true;  }
        else if ((c == 'e') || (c == 'E')) { return true;  }
        else if ((c == 'i') || (c == 'I')) { return true;  }
        else if ((c == 'o') || (c == 'O')) { return true;  }
        else if ((c == 'u') || (c == 'U')) { return true;  }
        else if ((c == 'y') || (c == 'Y')) { return true;  }
        else                               { return false; }
    }
}

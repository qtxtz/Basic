/****************************************************************************************************

BASIC! is an implementation of the Basic programming language for
Android devices.


Copyright (C) 2010 - 2013 Paul Laughton

This file is part of BASIC! for Android

    BASIC! is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BASIC! is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BASIC!.  If not, see <http://www.gnu.org/licenses/>.

    You may contact the author, Paul Laughton at basic@laughton.com
    
	*************************************************************************************************/


package com.rfo.basic;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class Search extends Activity {
	private String originalText;               // The original text
	private EditText rText;				// The replace text
    private String replaceText;
    private EditText sText;				// The search text
    private String searchText;
    private Button nextButton;			// The buttons
    private Button doneButton;
    private Button replaceButton;
    private Button replaceAllButton;
    private EditText theTextView;		//The EditText TextView
    private int Index;					//Indexes into the text
    private int nextIndex;

	private boolean mChanged = false;
	private boolean isSearchCaseSensitive = false;		// TODO: provide a control to set case-sensitivity

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)  {
    	// if BACK key restore original text
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
             Editor.DisplayText = originalText;
             Editor.mText.setText(Editor.DisplayText);
             Editor.mText.setSelection(Editor.selectionStart, Editor.selectionEnd);
             finish();
             return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setRequestedOrientation(Settings.getSreenOrientation(this));       
       originalText = Editor.DisplayText;		// Save in case of BACK Key

       this.setContentView(R.layout.search);  // Layouts xmls exist for both landscape or portrait modes
       
       this.rText = (EditText) findViewById(R.id.replace_text);          // The replace text
       this.sText = (EditText) findViewById(R.id.search_text);			 // The search for text

       this.nextButton = (Button) findViewById(R.id.next_button);		 // The buttons
       this.replaceButton = (Button) findViewById(R.id.replace_button);
       this.replaceAllButton = (Button) findViewById(R.id.replace_all_button);
       this.doneButton = (Button) findViewById(R.id.done_button);
       
       this.theTextView = (EditText) findViewById(R.id.the_text);		// The text display area
       theTextView.setTypeface(Typeface.MONOSPACE);
       theTextView.setText(Editor.DisplayText);							// The Editor's display text
//       theTextView.setTextColor(0xff000000);							// Background is white so text is black
       
   if (Settings.getEditorColor(this).equals("BW")){
	   theTextView.setTextColor(0xff000000);
	   theTextView.setBackgroundColor(0xffffffff);
	   rText.setTextColor(0xff000000);
	   rText.setBackgroundColor(0xffffffff);
	   theTextView.setTextColor(0xff000000);
	   theTextView.setBackgroundColor(0xffffffff);
   } else
     if (Settings.getEditorColor(this).equals("WB")){
    	 theTextView.setTextColor(0xffffffff);
    	 theTextView.setBackgroundColor(0xff000000);
    	 rText.setTextColor(0xffffffff);
    	 rText.setBackgroundColor(0xff000000);
    	 sText.setTextColor(0xffffffff);
    	 sText.setBackgroundColor(0xff000000);
   } else 
       if (Settings.getEditorColor(this).equals("WBL")){
    	   theTextView.setTextColor(0xffffffff);
    	   theTextView.setBackgroundColor(0xff006478);
    	   rText.setTextColor(0xffffffff);
    	   rText.setBackgroundColor(0xff006478);
    	   sText.setTextColor(0xffffffff);
    	   sText.setBackgroundColor(0xff006478);
         }  
   
   theTextView.setTextSize(1, Settings.getFont(this));

       
       // If there is a block of text selected in the Editor then make that
       // block of text the search for text
       
       if (Editor.selectionStart != Editor.selectionEnd){	
    	   int s = Editor.selectionStart;
    	   int e = Editor.selectionEnd;
    	   if (e < s) {
    		   s = e;
    		   e = Editor.selectionStart;
    	   }
    	   sText.setText(Editor.DisplayText.substring(s, e));
       }
       
       Index = -1;			// Current Index into text, set for nothing found
       nextIndex = 0;		// next Index
       
       
       this.nextButton.setOnClickListener(new OnClickListener() {		// ***** Next Button ****
           
           public void onClick(View v) {
        	   Editor.DisplayText = theTextView.getText().toString();   // Grab the text that the user is seeing
        	   															// She may edited it
        	   if (nextIndex < 0) nextIndex = 0;						// If nextIndex <0 then a previous search
        	   															// search has finished. Start next search
        	   															// from the start
        	   if (doNext()) return;									// If this next found something, return
        	   nextIndex = -1;											// Else indicate not found and (Index also -1 now)
        	   Toaster(searchText + " not found." );					// tell the user not found
        	   return;
           	}
       	});
       
       this.replaceButton.setOnClickListener(new OnClickListener() {	// ***** Replace Button ****
           
           public void onClick(View v) {
        	   if (Index <0){											// If nothing has been found....
            	   Toaster("Nothing found to replace" );
            	   return;
        	   }
        	   doReplace();												// else replace what was found
               return;
           	}
       	});

       this.replaceAllButton.setOnClickListener(new OnClickListener() {  // ******* Replace All Button *****
           
           public void onClick(View v) {
        	   doReplaceAll();
           	}
       	});
       
       this.doneButton.setOnClickListener(new OnClickListener() {		// **** Done Button ****
           
           public void onClick(View v) {
        	   Editor.DisplayText = theTextView.getText().toString();   // Grab the text that the user is seeing
        	   Editor.mText.setText(Editor.DisplayText);				// Set the Editor's EditText TextView text
				if (!mChanged) {
					mChanged = !originalText.equals(Editor.DisplayText);// She may have edited it
				}
				if (mChanged) {
					Basic.Saved = false;
				}
        	   if (nextIndex < 0 ) nextIndex = 0;						// If nextIndex indicates done, then set to start
        	   if (Index < 0) Index = 0;								// If Index indicates not found, set to start
        	   if (nextIndex < Index){
        		   int ni = nextIndex;
        		   nextIndex = Index;
        		   Index = ni;
        	   }
        	   Editor.mText.setSelection(Index, nextIndex);			    // Set the cursor or selection highlight
               finish();												// Done with this module
               return;
           	}
       	});

   
    }
    
    private boolean doNext(){										// Find the next occurrence of the search for string
 	   Index = nextIndex;											// Postion from last search becomes start of this search
	   searchText = sText.getText().toString();						// Get the search text from the dialog box
	   String MirrorText = "" + Editor.DisplayText;
		if (!isSearchCaseSensitive) {
			searchText = searchText.toLowerCase();
			MirrorText = MirrorText.toLowerCase();
		}
	   Index = MirrorText.indexOf(searchText, Index);				// Search for the text
	   if (Index < 0) return false;									// If not found, return false
	   nextIndex= Index + searchText.length();						// Set nextIndex to the end of the found text
	   theTextView.setSelection(Index, nextIndex);					// Highlight the selection
	   theTextView.setFocusable(true);								// Set focus on the text display area
	   theTextView.requestFocus();
	   return true;

    }
    
    private void doReplace(){										// Replace the text that was found
       if (nextIndex <0 || Index<0) return;							// Make sure we have valid indexes
 	   replaceText = rText.getText().toString();					// Get the replace text from the dialog
	   StringBuilder S = new StringBuilder(Editor.DisplayText);		// Schelpp text around to get needed method
	   S.replace(Index, nextIndex, replaceText);					// do the replace
	   mChanged = true;												// mark changed (even if it really didn't)
	   Editor.DisplayText = S.toString();							// Schelpp the text back
	   theTextView.setText(Editor.DisplayText);						
	   nextIndex= Index + replaceText.length();						// Set nextIndex after the replaced text
	   theTextView.setSelection(Index, nextIndex);					// Show the selection highlighted
	   theTextView.setFocusable(true);
	   theTextView.requestFocus();
    	
    }
    
    private void doReplaceAll(){
    	int count = 0;
    	Index = 0;
    	nextIndex = 0;
 	   	searchText = sText.getText().toString();				// Get the search text from the dialog box
 	   	int sl = searchText.length();
 	   	replaceText = rText.getText().toString();				// Get the replace text from the dialog
 	   	int rl = replaceText.length();
		String workingText = ("" + Editor.DisplayText);
		String mirrorText = workingText;
		StringBuilder S = new StringBuilder("");				// Workspace for building text with replacement

		if (!isSearchCaseSensitive) {
			searchText = searchText.toLowerCase();
			mirrorText = mirrorText.toLowerCase();
		}

 	   	do{
 	   		nextIndex = Index;									// Save last Index
			Index = mirrorText.indexOf(searchText,Index);		// Search
 	   		if (Index >=0){										// If found
				S.append(workingText.substring(nextIndex, Index)); // copy up to search text
				S.append(replaceText);							// insert replacement text
 	   			count = count + 1;								// Increment count
 	   			Index = Index + sl;								// Set new index
 	   		} 
 	   	}while (Index >= 0);									// Loop until not found

		if (count > 0) { mChanged = true; }						// Mark changed
		else           { rl = 0; }								// else set highlight length 0
		
		Index = S.length();										// End of last thing selected
		S.append(workingText.substring(nextIndex));				// Get the rest of the original string

		nextIndex = Index;										// Location in new string
		Index -= rl;											// of last thing selected

		Toaster(count + " items replaced");						// Tell the user how many replaced

		Editor.DisplayText = S.toString();						// Schelpp the text back
	    theTextView.setText(Editor.DisplayText);
	    theTextView.setSelection(Index, nextIndex);				// Select last thing selected
	    theTextView.setFocusable(true);
		theTextView.requestFocus();
    }
    
    private void Toaster(CharSequence msg){							// Tell the user "msg"
		  Context context = getApplicationContext();			    // via toast
		  CharSequence text = msg;
		  int duration = Toast.LENGTH_SHORT;
		  Toast toast = Toast.makeText(context, text, duration);
		  toast.setGravity(Gravity.TOP|Gravity.CENTER,0 , 0);
		  toast.show();
  }


}
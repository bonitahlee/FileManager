package com.bonita.filemanager.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.bonita.filemanager.R;
import com.bonita.filemanager.constants.BlazeKey;

import java.util.ArrayList;

/**
 * <pre>
 *     Blaze Prompt Dialog
 * </pre>
 *
 * @author parkho79
 * @date 2021-10-15
 */
public class BlazePromptDialog extends Dialog {

    public interface PromptStyle {
        int USERTYPE = 0;    //Custom Prompt
        int YESNO = 1;    //Prompt Yes | No
        int YESNOCANCEL = 2;    //Prompt Yes | No | Cancel
        int YESALLNO = 3;    //Prompt Yes | YesAll | No
        int ANYKEY = 4;
        int YESALLNOCANCEL = 5;    //Prompt Yes | YesAll | No | Cancel
        int NOYES = 6;    //Prompt No | Yes
    }

    public interface ResultKey {
        int YES = 0;
        int NO = 1;
        int CANCEL = 2;
    }

    public interface BlazePromptResultListener {
        void onResult(int a_selectKey);
    }

    public interface BlazePromptDismissListener {
        void onDismiss();
    }

    private Spinner mPromptSpinner;
    private ArrayAdapter<String> mArrayAdapter;
    private final ArrayList<String> mPromptList;

    private TextView mPromptLabel;

    private int mPromptType;

    private int mDefaultIndex;

    private BlazePromptResultListener mResultListener;

    private BlazePromptDismissListener mDismissListener;

    public BlazePromptDialog(Context a_context) {
        super(a_context);

        mPromptList = new ArrayList<>();
        mPromptType = PromptStyle.YESNO;

        bindLayout();
    }

    @Override
    public void show() {
        // Prompt list
        setPromptList();

        // Default index
        if (mDefaultIndex < mPromptList.size()) {
            mPromptSpinner.post(new Runnable() {
                public void run() {
                    mPromptSpinner.setSelection(mDefaultIndex);
                }
            });
        }

        Rect realSize = new Rect();
        mPromptLabel.getPaint().getTextBounds(mPromptLabel.getText().toString(), 0, mPromptLabel.getText().length(), realSize);

        int totalItemsWidth = 0;
        if (mArrayAdapter != null) {
            int numberOfItems = mArrayAdapter.getCount();
            // Get total height of all items.
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                //[20.04.06][jangkh][BRAILLESNV-16936] UI 구성 시, lock up 방어 코드 추가.
                try {
                    View item = mArrayAdapter.getView(itemPos, null, mPromptSpinner);
                    float px = 500 * (mPromptSpinner.getResources().getDisplayMetrics().density);
                    item.measure(View.MeasureSpec.makeMeasureSpec((int) px, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    if (item.getMeasuredWidth() > totalItemsWidth) {
                        totalItemsWidth = item.getMeasuredWidth();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        //[20.08.27][jangkh][BRAILLESNV-17245] prompt 선택 크기 변경.
        getWindow().setLayout(realSize.width() + totalItemsWidth + 200, WindowManager.LayoutParams.WRAP_CONTENT);

        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mDismissListener != null) {
            mDismissListener.onDismiss();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent a_keyEvent) {
        final int keyCode = a_keyEvent.getKeyCode();

        if (a_keyEvent.getAction() == KeyEvent.ACTION_UP) {
            if (keyCode == BlazeKey.CONTROL_CANCEL) {
                cancel();
                return true;
            }

            if (keyCode == BlazeKey.NAVIGATION_OK) {
                mResultListener.onResult(mPromptSpinner.getSelectedItemPosition());
                dismiss();
                return true;
            }
        }

        return super.dispatchKeyEvent(a_keyEvent);
    }

    /**
     * Widget 에 focus 시 TTS
     */
    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent a_event) {
        if (isShowing() == false) {
            return super.dispatchPopulateAccessibilityEvent(a_event);
        }

        final int eventType = a_event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return true;
        }

        return super.dispatchPopulateAccessibilityEvent(a_event);
    }

    /**
     * Layout 설정
     */
    private void bindLayout() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_prompt, null);
        setContentView(view);

        mPromptLabel = (TextView) findViewById(R.id.tv_label);
        mPromptLabel.setSingleLine();

        mArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, mPromptList);
        mPromptSpinner = (Spinner) findViewById(R.id.spinner_prompt);
        mPromptSpinner.setAdapter(mArrayAdapter);
    }

    /**
     * User type prompt 설정.
     */
    public void addPrompt(String a_strMsg) {
        mPromptList.add(a_strMsg);
    }

    /**
     * Prompt 설정
     */
    public void setPrompt(String a_strMsg, int a_promptType, int a_defaultIndex) {
        mPromptLabel.setText(a_strMsg);
        mPromptType = a_promptType;
        mDefaultIndex = a_defaultIndex;
    }

    private void setPromptList() {
        switch (mPromptType) {
            // Yes(Y) | No(N)
            case PromptStyle.YESNO:
                mPromptList.add(getContext().getString(R.string.COMMON_MSG_PROMPT_YES));
                mPromptList.add(getContext().getString(R.string.COMMON_MSG_PROMPT_NO));
                break;

            // No(N)(Y) | Yes(Y)
            case PromptStyle.NOYES:
                mPromptList.add(getContext().getString(R.string.COMMON_MSG_PROMPT_NO));
                mPromptList.add(getContext().getString(R.string.COMMON_MSG_PROMPT_YES));
                break;

            // Yes(Y) | No(N) | Cancel(C)
            case PromptStyle.YESNOCANCEL:
                mPromptList.add(getContext().getString(R.string.COMMON_MSG_PROMPT_YES));
                mPromptList.add(getContext().getString(R.string.COMMON_MSG_PROMPT_NO));
                mPromptList.add(getContext().getString(R.string.COMMON_MSG_CANCEL));
                break;

            // Yes(Y) | Yes All(A) | No(No)
            case PromptStyle.YESALLNO:
                mPromptList.add(getContext().getString(R.string.COMMON_MSG_PROMPT_YES));
                mPromptList.add(getContext().getString(R.string.COMMON_MSG_PROMPT_YES_TO_ALL));
                mPromptList.add(getContext().getString(R.string.COMMON_MSG_PROMPT_NO));
                break;

            // Yes(Y) | Yes All(A) | No(N) | Cancel(C)
            case PromptStyle.YESALLNOCANCEL:
                mPromptList.add(getContext().getString(R.string.COMMON_MSG_PROMPT_YES));
                mPromptList.add(getContext().getString(R.string.COMMON_MSG_PROMPT_YES_TO_ALL));
                mPromptList.add(getContext().getString(R.string.COMMON_MSG_PROMPT_NO));
                mPromptList.add(getContext().getString(R.string.COMMON_MSG_CANCEL));
                break;

            default:
                break;
        }

        // List 반영
        mArrayAdapter.notifyDataSetChanged();
    }

    /**
     * 유효한 key 확인
     */
    private boolean isValidKey(KeyEvent a_keyEvent) {
        final int keyCode = a_keyEvent.getKeyCode();
        switch (keyCode) {
            case BlazeKey.NAVIGATION_LEFT:
            case BlazeKey.NAVIGATION_RIGHT:
            case BlazeKey.NAVIGATION_OK:
            case BlazeKey.FUNCTION_MODE_SHORT:
                return true;

            default:
                return false;
        }
    }

    /**
     * Result listener
     */
    public void setResultListener(BlazePromptResultListener listener) {
        mResultListener = listener;
    }

    /**
     * Dismiss listener
     */
    public void setOnPromptDismissListener(BlazePromptDismissListener listener) {
        mDismissListener = listener;
    }
}

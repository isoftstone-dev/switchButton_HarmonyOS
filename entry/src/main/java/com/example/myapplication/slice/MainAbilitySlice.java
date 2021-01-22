package com.example.myapplication.slice;

import com.example.myapplication.ResourceTable;
import com.isoftstone.switchbutton.SwitchButton;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.*;
import ohos.agp.window.dialog.ToastDialog;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class MainAbilitySlice extends AbilitySlice implements Component.ClickedListener {
    private static HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x000111,"SwitchButton");
    private ScrollView scrollView;
    private Button mbtn_toggle_ani,mbtn_toggle_ani_no_event,mbtn_toggle_not_ani,mbtn_toggle_not_ani_no_event;
    private SwitchButton mListenerSb, mListenerDistinguishSb, mLongSb, mToggleSb, mCheckedSb, mDelaySb, mForceOpenSb, mForceOpenControlSb;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);

//        SwitchButton button = (SwitchButton) findComponentById(ResourceTable.Id_switchBtn1);
        initView();
    }

    private void initView(){
        findView();
        // work with listener
        mListenerSb.setCheckedStateChangedListener(new AbsButton.CheckedStateChangedListener(){
            @Override
            public void onCheckedChanged(AbsButton absButton, boolean isChecked) {
//                mListenerFinish.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
                if (mListenerDistinguishSb.isChecked() != isChecked) {
                    mListenerDistinguishSb.setChecked(isChecked);
                }
            }

        });


        // check in check
        mForceOpenSb.setCheckedStateChangedListener(new AbsButton.CheckedStateChangedListener(){
            @Override
            public void onCheckedChanged(AbsButton absButton, boolean isChecked) {
//                mListenerFinish.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
                if (mForceOpenControlSb.isChecked()) {
//                    toast("Call mForceOpenSb.setChecked(true); in on CheckedChanged");
                    mForceOpenSb.setChecked(true);
                }
            }

        });


        // check in check
        mToggleSb.setCheckedStateChangedListener(new AbsButton.CheckedStateChangedListener(){
            @Override
            public void onCheckedChanged(AbsButton absButton, boolean isChecked) {
//                mListenerFinish.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
                HiLog.info(label,"Toggle SwitchButton new check state: " + (isChecked ? "Checked" : "Unchecked"));
                toast("Toggle SwitchButton new check state: " + (isChecked ? "Checked" : "Unchecked"));
            }

        });


    }

    private void findView() {
        scrollView = (ScrollView) findComponentById(ResourceTable.Id_scrollview);
        mListenerSb = (SwitchButton) findComponentById(ResourceTable.Id_switchButton3_1);
        mListenerDistinguishSb = (SwitchButton) findComponentById(ResourceTable.Id_switchButton3_2);
        mForceOpenSb =  (SwitchButton) findComponentById(ResourceTable.Id_switchButton3_focus_open);
        mForceOpenControlSb =  (SwitchButton) findComponentById(ResourceTable.Id_switchButton3_focus_open_control);

        mToggleSb = (SwitchButton) findComponentById(ResourceTable.Id_switchButton3_use_toggle);
        mbtn_toggle_ani = (Button) findComponentById(ResourceTable.Id_toggle_ani);
        mbtn_toggle_ani.setClickedListener(this::onClick);
        mbtn_toggle_ani_no_event = (Button) findComponentById(ResourceTable.Id_toggle_ani_no_event);
        mbtn_toggle_ani_no_event.setClickedListener(this::onClick);
        mbtn_toggle_not_ani = (Button) findComponentById(ResourceTable.Id_toggle_not_ani);
        mbtn_toggle_not_ani.setClickedListener(this::onClick);
        mbtn_toggle_not_ani_no_event = (Button) findComponentById(ResourceTable.Id_toggle_not_ani_no_event);
        mbtn_toggle_not_ani_no_event.setClickedListener(this::onClick);


    }



    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    private void toast(String str){
        new ToastDialog(getContext())
                .setText(str)
                .show();
    }

    @Override
    public void onClick(Component component) {
        switch (component.getId()){
            case ResourceTable.Id_toggle_ani:
                HiLog.info(label,"click Id_toggle_ani");
               try{ mToggleSb.toggle();}
               catch(Exception e){
                   HiLog.info(label,"Exception:"+e.toString());
               }
                break;
            case ResourceTable.Id_toggle_ani_no_event:
                mToggleSb.toggleNoEvent();
                break;
            case ResourceTable.Id_toggle_not_ani:
                mToggleSb.toggleImmediately();
                break;
            case ResourceTable.Id_toggle_not_ani_no_event:
                mToggleSb.toggleImmediatelyNoEvent();
                break;
            default:
                break;
        }
    }
}

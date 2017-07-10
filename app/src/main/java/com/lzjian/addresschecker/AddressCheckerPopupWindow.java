package com.lzjian.addresschecker;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressCheckerPopupWindow {

    private final String TAG = getClass().getSimpleName();
    private static final int MAX_CHECKER_NUM = 6;
    private Context mContext;
    private PopupWindow mPopupWindow;
    private OnListViewItemClickListener onListViewItemClickListener;
    private DismissListener dismissListener;
    private OnCommitListener onCommitListener;

    private View popupWindow_view;
    private TextView tv_commit;
    private TextView tv_cancel;
    private TextView tv_result;
    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    private TextView tv4;
    private TextView tv5;
    private TextView tv6;
    private List<TextView> tvs = new ArrayList<TextView>();
    private ListView lv;
    private MyAdapter adapter;
    private ImageView iv_cursor;

    private int screenWidth = 0;
    private int barWidth = 0;//游标宽度
    private int offset = 0;//游标图片偏移量
    private int imgBarInitialPoint = 0; // 滑动条的起始位置
    private int pageNum = 0;//当前页面编号
    private int onePageOffset = 0;// 页卡1 -> 页卡2 偏移量
    private int twoPageOffset = 0;// 页卡1 -> 页卡3 偏移量
    private int threePageOffset = 0; // 页卡1 -> 页卡4 偏移量
    private int fourPageOffset = 0; // 页卡1 -> 页卡5 偏移量
    private int fivePageOffset = 0; // 页卡1 -> 页卡6 偏移量

    private SparseIntArray offsetMap;
    private Map<String, String> addressMap;

    public AddressCheckerPopupWindow(Context context) {
        mContext = context;
        initView();
    }

    private void initCursorPos(int pageNum) {
        // 初始化动画
        if (barWidth == 0){
            barWidth = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_bar).getWidth();// 获取图片宽度
        }
        if (offset == 0){
            DisplayMetrics dm = new DisplayMetrics();//初始化DisplayMetrics对象
            ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);//将当前窗口信息放入DisplayMetrics类中
            screenWidth = dm.widthPixels;// 获取分辨率宽度
            offset = (screenWidth / MAX_CHECKER_NUM - barWidth) / 2;// 计算偏移量(保证滑动条在该tab下正中间), 3就是把屏幕分成3等分
        }
        if (onePageOffset == 0){
            onePageOffset = offset * 2 + barWidth;
        }
        if (twoPageOffset == 0){
            twoPageOffset = onePageOffset * 2;
        }
        if (threePageOffset == 0){
            threePageOffset = onePageOffset * 3;
        }
        if (fourPageOffset == 0){
            fourPageOffset = onePageOffset * 4;
        }
        if (fivePageOffset == 0){
            fivePageOffset = onePageOffset * 5;
        }
        if (offsetMap == null){
            offsetMap = new SparseIntArray();
            offsetMap.put(0, 0);
            offsetMap.put(1, onePageOffset);
            offsetMap.put(-1, -onePageOffset);
            offsetMap.put(2, twoPageOffset);
            offsetMap.put(-2, -twoPageOffset);
            offsetMap.put(3, threePageOffset);
            offsetMap.put(-3, -threePageOffset);
            offsetMap.put(4, fourPageOffset);
            offsetMap.put(-4, -fourPageOffset);
            offsetMap.put(5, fivePageOffset);
            offsetMap.put(-5, -fivePageOffset);
        }
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset + offsetMap.get(pageNum), 0);
        iv_cursor.setImageMatrix(matrix);// 设置动画初始位置
    }

    /** * 弹出Popupwindow */
    private void initView() {
        popupWindow_view = LayoutInflater.from(mContext).inflate(R.layout.ppw_address_checker, null);
        tv_cancel = (TextView) popupWindow_view.findViewById(R.id.tv_cancel);
        tv_commit = (TextView) popupWindow_view.findViewById(R.id.tv_commit);
        tv_result = (TextView) popupWindow_view.findViewById(R.id.tv_result);
        tv1 = (TextView) popupWindow_view.findViewById(R.id.tv1);
        tv2 = (TextView) popupWindow_view.findViewById(R.id.tv2);
        tv3 = (TextView) popupWindow_view.findViewById(R.id.tv3);
        tv4 = (TextView) popupWindow_view.findViewById(R.id.tv4);
        tv5 = (TextView) popupWindow_view.findViewById(R.id.tv5);
        tv6 = (TextView) popupWindow_view.findViewById(R.id.tv6);
        tvs.add(tv1);
        tvs.add(tv2);
        tvs.add(tv3);
        tvs.add(tv4);
        tvs.add(tv5);
        tvs.add(tv6);
        iv_cursor = (ImageView) popupWindow_view.findViewById(R.id.iv_cursor);
        lv = (ListView) popupWindow_view.findViewById(R.id.lv);

        adapter = new MyAdapter(mContext, null);
        initCursorPos(imgBarInitialPoint);
        addressMap = new HashMap<String, String>();
        mPopupWindow = new PopupWindow(popupWindow_view, ViewGroup.LayoutParams.MATCH_PARENT, DensityUtils.dip2px(mContext, 320));
        mPopupWindow.setAnimationStyle(R.style.ppw_anim_bottom_style);
        // setBackgroundDrawable这句一定要加,加了之后点击除popupWindow的其他区域会dismiss popupWindow
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        // setFocusable(false)让除popupWindow的其他区域可以获取焦点
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setWindowAlpha(false);
                if (dismissListener != null){
                    dismissListener.dismiss();
                }
            }
        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        tv_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onCommitListener != null){
                    onCommitListener.onCommit(addressMap, tv_result.getText().toString().trim());
                }
            }
        });
        for (int i=0;i<tvs.size();i++){
            final int finalI = i;
            tvs.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int j=0;j<tvs.size();j++){
                        if (j != finalI){
                            tvs.get(j).setTextColor(mContext.getResources().getColor(R.color.gray979da5));
                        }else{
                            tvs.get(j).setTextColor(mContext.getResources().getColor(R.color.blue5d9ff5));
                        }
                    }
                    slide(finalI);
                }
            });
        }
    }

    private void slide(int toWhere) {
        Animation animation = null;
        // float fromXDelta,这个参数表示动画开始的点离当前View X坐标上的差值; (当前View指的是ImageView的初始点,在这里指的是imgBarInitialPoint)
        // float toXDelta, 这个参数表示动画结束的点离当前View X坐标上的差值;
        // float fromYDelta, 这个参数表示动画开始的点离当前View Y坐标上的差值;
        // float toYDelta,这个参数表示动画开始的点离当前View Y坐标上的差值;
        animation = new TranslateAnimation(offsetMap.get(pageNum - imgBarInitialPoint), offsetMap.get(toWhere - imgBarInitialPoint), 0, 0);
        pageNum = toWhere;
        animation.setFillAfter(true);// True:图片停在动画结束位置
        animation.setDuration(300); // 动画时长不会阻断UI进程的
        iv_cursor.startAnimation(animation);
    }

    /** * 显示PopupWindow */
    public void show() {
        if (mPopupWindow != null && !mPopupWindow.isShowing()) {
            mPopupWindow.showAtLocation(popupWindow_view, Gravity.BOTTOM, 0 ,0);
            setWindowAlpha(true);
            imgBarInitialPoint = pageNum;
            initCursorPos(imgBarInitialPoint);
            slide(pageNum);
        }
    }

    /** * 消失PopupWindow */
    public void dismiss() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    public boolean isShowing() {
        return mPopupWindow != null && mPopupWindow.isShowing();
    }

    private void setWindowAlpha(boolean isOpen) {
        WindowAlphaUtils.setWindowAlpha(mContext, isOpen);
    }

    public interface DismissListener {
        void dismiss();
    }

    public interface OnCommitListener{
        void onCommit(Map<String, String> map, String str);
    }

    public void setDismissListener(DismissListener dismisslistener) {
        this.dismissListener = dismisslistener;
    }

    public void setOnCommitListener(OnCommitListener onCommitListener){
        this.onCommitListener = onCommitListener;
    }

    public interface OnListViewItemClickListener {
        void onItemClick(AdapterView<?> parent, View view, int position, long id);
    }

    public void setOnListViewItemClickListener(OnListViewItemClickListener onListViewItemClickListener) {
        this.onListViewItemClickListener = onListViewItemClickListener;
    }
}

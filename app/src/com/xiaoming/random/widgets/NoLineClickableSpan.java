package com.xiaoming.random.widgets;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

/**
 * Created by XIAOM on 2015/3/3.
 */
public class NoLineClickableSpan extends ClickableSpan {
    String text;
    Context context;

    public NoLineClickableSpan(Context context, String text) {
        super();
        this.text = text;
        this.context = context;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(ds.linkColor);
        ds.setUnderlineText(false); //去掉下划线
    }

    @Override
    public void onClick(View widget) {
        processHyperLinkClick(text); //点击超链接时调用
    }

    private void processHyperLinkClick(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        context.startActivity(intent);
    }
}

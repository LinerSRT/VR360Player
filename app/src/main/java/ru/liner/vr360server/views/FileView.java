package ru.liner.vr360server.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;



import java.io.IOException;

import ru.liner.vr360server.R;
import ru.liner.vr360server.utils.FileUtils;


public class FileView extends BaseItem {
    private ImageView fileTypeIcon;
    private TextView fileName;
    private TextView fileSize;
    private ImageButton deleteIcon;

    public FileView(Context context) {
        super(context);
    }

    public FileView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onInflate() {
        inflater.inflate(R.layout.file_item, this);
    }

    @Override
    protected void onFindViewById() {
        fileTypeIcon = findViewById(R.id.fileTypeView);
        fileName = findViewById(R.id.fileName);
        fileSize = findViewById(R.id.fileSize);
        deleteIcon = findViewById(R.id.deleteIcon);
        hideView();
    }


    public void setFileName(String text) {
        fileName.setText(text);
    }

    public void setFileSize(String text) {
        fileSize.setText(text);
    }

    public void setFileType(String fileType){
        try {
            fileTypeIcon.setImageDrawable(FileUtils.getFileIcon(getContext(), fileType));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ImageButton getDeleteButton(){
        return deleteIcon;
    }
}

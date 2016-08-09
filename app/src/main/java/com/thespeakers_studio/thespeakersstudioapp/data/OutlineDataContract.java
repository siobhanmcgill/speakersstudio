package com.thespeakers_studio.thespeakersstudioapp.data;

import android.provider.BaseColumns;

/**
 * Created by smcgi_000 on 8/9/2016.
 */
public class OutlineDataContract extends BasicDataContract {

    OutlineDataContract() {}

    public static abstract class OutlineItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "outline_item";
        public static final String COLUMN_NAME_OUTLINE_ITEM_ID = "outline_item_id";

        public static final String COLUMN_NAME_PRESENTATION_ID =
                PresentationDataContract.PresentationEntry.COLUMN_NAME_PRESENTATION_ID;
        public static final String COLUMN_NAME_ANSWER_ID =
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_ID;

        public static final String COLUMN_NAME_ORDER = "order";
        public static final String COLUMN_NAME_DURATION = "duration";
        public static final String COLUMN_NAME_TEXT = "text";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE" + OutlineItemEntry.TABLE_NAME + " (" +
                        OutlineItemEntry._ID + " INTEGER PRIMARY KEY, " +
                        OutlineItemEntry.COLUMN_NAME_OUTLINE_ITEM_ID + TEXT_TYPE + COMMA_SEP +
                        OutlineItemEntry.COLUMN_NAME_PRESENTATION_ID + TEXT_TYPE + COMMA_SEP +
                        OutlineItemEntry.COLUMN_NAME_ANSWER_ID + TEXT_TYPE + COMMA_SEP +

                        OutlineItemEntry.COLUMN_NAME_ORDER + INT_TYPE + COMMA_SEP +
                        OutlineItemEntry.COLUMN_NAME_DURATION + INT_TYPE + COMMA_SEP +
                        OutlineItemEntry.COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +

                        COLUMN_NAME_CREATED_BY + TEXT_TYPE + COMMA_SEP +
                        COLUMN_NAME_DATE_CREATED + TEXT_TYPE + COMMA_SEP +
                        COLUMN_NAME_MODIFIED_BY + TEXT_TYPE + COMMA_SEP +
                        COLUMN_NAME_DATE_MODIFIED + TEXT_TYPE + COMMA_SEP +
                        " )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + OutlineItemEntry.TABLE_NAME;

        /*
            _id
            outline_item_id
            presentation_id
            answer_id
            order
            duration
            text

            date_created
            date_modified
            created_by
            modified_by
         */
    }
}
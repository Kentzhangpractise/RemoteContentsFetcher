package com.pactera.kent.remotecontentsfetcher.json;

import java.util.List;

/**
 * Created by Kent on 2015/3/18.
 */
public class JsonContentsDataStructure {

    public String title;
    public List<rows> rows;

    public class rows {
        public String title;
        public String description;
        public String imageHref;
    }
}

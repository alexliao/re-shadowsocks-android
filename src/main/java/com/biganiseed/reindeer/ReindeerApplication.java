package com.biganiseed.reindeer;

import com.github.shadowsocks.ReindeerUtils;
import com.github.shadowsocks.ShadowsocksApplication;

public class ReindeerApplication extends ShadowsocksApplication {

    @Override
    public void onCreate (){
        com.github.shadowsocks.utils.Path.setBASE(ReindeerUtils.getExecPath(this));
    }

}

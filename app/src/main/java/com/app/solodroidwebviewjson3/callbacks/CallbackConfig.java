package com.app.solodroidwebviewjson3.callbacks;

import com.app.solodroidwebviewjson3.models.Ads;
import com.app.solodroidwebviewjson3.models.Apps;
import com.app.solodroidwebviewjson3.models.Menu;
import com.app.solodroidwebviewjson3.models.MoreApps;

import java.util.ArrayList;
import java.util.List;

public class CallbackConfig {
    public List<Apps> apps = new ArrayList<>();
    public List<Menu> menus = new ArrayList<>();
    public List<Ads> ads = new ArrayList<>();
    public List<MoreApps> more_apps = new ArrayList<>();

}
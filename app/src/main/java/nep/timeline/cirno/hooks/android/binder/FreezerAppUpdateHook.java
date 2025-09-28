package nep.timeline.cirno.hooks.android.binder;

import de.robv.android.xposed.XC_MethodHook;
import nep.timeline.cirno.entity.AppRecord;
import nep.timeline.cirno.framework.AbstractMethodHook;
import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.services.AppService;
import nep.timeline.cirno.services.BinderService;
import nep.timeline.cirno.services.FreezerService;
import nep.timeline.cirno.services.ProcessService;
import nep.timeline.cirno.utils.SystemChecker;
import nep.timeline.cirno.virtuals.ProcessRecord;

public class FreezerAppUpdateHook extends MethodHook {
    public FreezerAppUpdateHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return "com.android.server.am.OomAdjuster";
    }

    @Override
    public String getTargetMethod() {
        return "updateAppFreezeStateLSP";
    }

    @Override
    public Object[] getTargetParam() {
        return new Object[] { "com.android.server.am.ProcessRecord", int.class, boolean.class, int.class };
    }

    @Override
    public XC_MethodHook getTargetHook() {
        return new AbstractMethodHook() {
            @Override
            protected void beforeMethod(MethodHookParam param) {
                Object app = param.args[0];
                if(app != null){
                    ProcessRecord pr = ProcessService.getProcessRecord(app);
                    if (pr != null) {
                        AppRecord ar = AppService.get(pr.getPackageName(), pr.getUserId());
                        if(ar != null && !ar.isSystem()){
                            param.setResult(null);
                        }
                    }
                }
            }
        };
    }

    @Override
    public boolean isIgnoreError() {
        return true;
    }
}

package nep.timeline.cirno.hooks.android.broadcast;

import android.os.Build;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import nep.timeline.cirno.framework.AbstractMethodHook;
import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.log.Log;
import nep.timeline.cirno.services.ProcessService;
import nep.timeline.cirno.utils.SystemChecker;
import nep.timeline.cirno.virtuals.BroadcastRecord;
import nep.timeline.cirno.virtuals.ProcessRecord;

public class BroadcastDeliveryHook extends MethodHook {
    public BroadcastDeliveryHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return "com.android.server.am.BroadcastQueueModernImpl";
    }

    @Override
    public String getTargetMethod() {
        return "shouldSkipReceiver";
    }

    @Override
    public Object[] getTargetParam() {
        return new Object[] { "com.android.server.am.BroadcastProcessQueue", "com.android.server.am.BroadcastRecord", int.class };
    }

    @Override
    public XC_MethodHook getTargetHook() {
        return new AbstractMethodHook() {
            @Override
            protected void beforeMethod(XC_MethodHook.MethodHookParam param) {

                Object queue = param.args[0];
                if (queue == null)
                    return;
                Object app = XposedHelpers.getObjectField(queue, "app");
                if (app == null)
                    return;

                ProcessRecord processRecord = ProcessService.getProcessRecord(app);
                if (processRecord == null)
                    return;

                if (processRecord.isFrozen()) {
                    //Log.d("skip broadcast from " + processRecord.getPid());
                    param.setResult("already terminal state");
                }
            }
        };
    }
}

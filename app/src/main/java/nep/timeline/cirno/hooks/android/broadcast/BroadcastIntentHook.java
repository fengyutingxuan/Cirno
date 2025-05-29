package nep.timeline.cirno.hooks.android.broadcast;

import android.content.Intent;
import android.os.Build;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import nep.timeline.cirno.GlobalVars;
import nep.timeline.cirno.entity.AppRecord;
import nep.timeline.cirno.framework.AbstractMethodHook;
import nep.timeline.cirno.log.Log;
import nep.timeline.cirno.services.AppService;
import nep.timeline.cirno.services.FreezerService;

public class BroadcastIntentHook {
    public BroadcastIntentHook(ClassLoader classLoader) {
       try {
           Class<?> clazz = XposedHelpers.findClassIfExists("com.android.server.am.ActivityManagerService", classLoader);

           if (clazz == null) {
               Log.e("无法监听广播意图1!");
               return;
           }

           String methodName = "broadcastIntentLocked";

           Method targetMethod = null;
           for (Method method : clazz.getDeclaredMethods())
               if (method.getName().equals(methodName) && (targetMethod == null || targetMethod.getParameterTypes().length < method.getParameterTypes().length))
                   targetMethod = method;

           if (targetMethod == null) {
               Log.e("无法监听广播意图2!");
               return;
           }

           XposedBridge.hookMethod(targetMethod, new AbstractMethodHook() {
               @Override
               protected void beforeMethod(MethodHookParam param) {
                   int intentArgsIndex = 3;

                   int userIdIndex = 20;

                   Intent intent = (Intent) param.args[intentArgsIndex];
                   int userId = (int) param.args[userIdIndex];
                   if (intent != null) {
                       String action = intent.getAction();

                       if (action == null || !action.endsWith(".android.c2dm.intent.RECEIVE"))
                           return;

                       String packageName = (intent.getComponent() == null ? intent.getPackage() : intent.getComponent().getPackageName());

                       if (packageName == null)
                           return;

                       AppRecord appRecord = AppService.get(packageName, userId);
                       if (appRecord == null)
                           return;

                       FreezerService.temporaryUnfreezeIfNeed(appRecord, "FCM PUSH", 1500);
                   }
               }
           });

           Log.i("监听广播意图");
       } catch (Throwable throwable) {
           XposedBridge.log(GlobalVars.TAG + " -> 无法监听广播意图, 异常:");
           XposedBridge.log(throwable);
           Log.e("监听广播意图失败", throwable);
       }
    }
}
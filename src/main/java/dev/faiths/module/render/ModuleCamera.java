package dev.faiths.module.render;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.MotionEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueInt;
import org.lwjgl.input.Mouse;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleCamera extends CheatModule {

	public ValueBoolean freelook = new ValueBoolean("FreeLook", false);
	public ValueBoolean viewclip = new ValueBoolean("ViewClip", false);
	public final ValueInt dist = new ValueInt("ClipDistance", 1, 0, 10).visible(()->viewclip.getValue());
	public ValueBoolean nohurtcam = new ValueBoolean("NoHurtCam", false);


	private boolean released;

	public ModuleCamera() {
		super("Camera", Category.RENDER);
	}

	private final Handler<MotionEvent> motionEventHandler = event -> {
		if(event.getEventState() == MotionEvent.EventState.POST){
			if(freelook.getValue()) {
				if (Mouse.isButtonDown(2)) {
					mc.gameSettings.thirdPersonView = 1;
					released = false;
				} else {
					if (!released) {
						mc.gameSettings.thirdPersonView = 0;
						released = true;
					}
				}
			}
		}
	};

	public boolean getViewClip() {
		return viewclip.getValue();
	}

	public boolean getNoHurtCam() {
		return nohurtcam.getValue();
	}

	public boolean getMcLock() {
		return freelook.getValue();
	}
}

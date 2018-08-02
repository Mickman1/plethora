package org.squiddev.plethora.gameplay.modules.glasses;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;

import java.util.Arrays;

public class MessageCanvasAdd implements IMessage {
	private int canvasId;
	private BaseObject[] objects;

	public MessageCanvasAdd(int canvasId, BaseObject[] objects) {
		this.canvasId = canvasId;
		this.objects = objects;
	}

	public MessageCanvasAdd() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		canvasId = buf.readInt();

		int size = buf.readInt();
		BaseObject[] objects = this.objects = new BaseObject[size];
		for (int i = 0; i < size; i++) {
			objects[i] = ObjectRegistry.read(buf);
		}

		// We sort on ID in order to guarantee parents are loaded before their children
		Arrays.sort(objects, BaseObject.SORTING_ORDER);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(canvasId);

		buf.writeInt(objects.length);
		for (BaseObject object : objects) {
			ObjectRegistry.write(buf, object);
		}
	}

	public static class Handler implements IMessageHandler<MessageCanvasAdd, IMessage> {

		@Override
		public IMessage onMessage(MessageCanvasAdd message, MessageContext context) {
			CanvasClient canvas = new CanvasClient(message.canvasId);

			for (BaseObject obj : message.objects) canvas.updateObject(obj);
			CanvasHandler.addClient(canvas);
			return null;
		}
	}
}

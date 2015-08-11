package com.hitchh1k3rsguide.$CORE_REPLACE$.hitchcore;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageCoreGeneric implements IMessage
{

    public enum MESSAGE_TYPE
    {
        PRINT_CHANGES;

        public static MESSAGE_TYPE fromOrdinal(int o)
        {
            for (MESSAGE_TYPE type : MESSAGE_TYPE.values())
            {
                if (type.ordinal() == o)
                {
                    return type;
                }
            }
            return null;
        }
    }

    private MESSAGE_TYPE type;

    public MessageCoreGeneric()
    {
    }

    public MessageCoreGeneric(MESSAGE_TYPE type)
    {
        this.type = type;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(type.ordinal());
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        type = MESSAGE_TYPE.fromOrdinal(buf.readInt());
    }

    public static class Handler implements IMessageHandler<MessageCoreGeneric, IMessage>
    {

        @Override
        public IMessage onMessage(MessageCoreGeneric message, MessageContext ctx)
        {
            if (message.type == MESSAGE_TYPE.PRINT_CHANGES && ctx.side.isClient())
            {
                CoreUtils.printChanges(null);
            }
            return null;
        }

    }

}

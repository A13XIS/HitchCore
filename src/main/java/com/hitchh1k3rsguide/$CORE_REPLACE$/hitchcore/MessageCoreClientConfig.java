package com.hitchh1k3rsguide.$CORE_REPLACE$.hitchcore;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageCoreClientConfig implements IMessage
{

    private String category;
    private String name;
    private int    type;
    private Object value;

    public MessageCoreClientConfig()
    {
    }

    public MessageCoreClientConfig(String category, String name, int type, Object value)
    {
        this.category = category;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        Serializer.writeString(buf, category);
        Serializer.writeString(buf, name);
        buf.writeInt(type);
        switch (CoreConfig.ConfigType.byID(type))
        {
            case BOOLEAN:
                buf.writeBoolean((Boolean) value);
                break;
            case BOOLEAN_ARRAY:
                Serializer.writeBooleanArray(buf, (boolean[]) value);
                break;
            case DOUBLE:
                buf.writeDouble((Double) value);
                break;
            case DOUBLE_ARRAY:
                Serializer.writeDoubleArray(buf, (double[]) value);
                break;
            case INTEGER:
                buf.writeInt((Integer) value);
                break;
            case INTEGER_ARRAY:
                Serializer.writeIntArray(buf, (int[]) value);
                break;
            case STRING:
                Serializer.writeString(buf, (String) value);
                break;
            case STRING_ARRAY:
                Serializer.writeStringArray(buf, (String[]) value);
                break;
        }
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        category = Serializer.readString(buf);
        name = Serializer.readString(buf);
        type = buf.readInt();
        switch (CoreConfig.ConfigType.byID(type))
        {
            case BOOLEAN:
                value = buf.readBoolean();
                break;
            case BOOLEAN_ARRAY:
                value = Serializer.readBooleanArray(buf);
                break;
            case DOUBLE:
                value = buf.readDouble();
                break;
            case DOUBLE_ARRAY:
                value = Serializer.readDoubleArray(buf);
                break;
            case INTEGER:
                value = buf.readInt();
                break;
            case INTEGER_ARRAY:
                value = Serializer.readIntArray(buf);
                break;
            case STRING:
                value = Serializer.readString(buf);
                break;
            case STRING_ARRAY:
                value = Serializer.readStringArray(buf);
                break;
        }
    }

    public static class Handler implements IMessageHandler<MessageCoreClientConfig, IMessage>
    {

        @Override
        public IMessage onMessage(MessageCoreClientConfig message, MessageContext ctx)
        {
            for (CoreConfig.ModConfigOption option : CoreConfig.configOptions)
            {
                if (message.category.equals(option.category))
                {
                    CoreConfig.setProperty(message.category, message.name, message.value);
                    return null;
                }
            }
            return null;
        }

    }

}

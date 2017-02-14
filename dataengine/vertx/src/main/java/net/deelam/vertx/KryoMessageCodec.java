package net.deelam.vertx;

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageCodec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class KryoMessageCodec<C> implements MessageCodec<C,C> {
  
  public synchronized static <C> KryoMessageCodec<C> register(EventBus eb, Class<C> beanClass){
    try {
      // throws error if already registered
      KryoMessageCodec<C> codec = new KryoMessageCodec<C>(beanClass);
      eb.registerDefaultCodec(beanClass, codec);
      return codec;
    } catch (IllegalStateException e) {
      //log.error(e.getMessage()); // log it and keep going
      return null;
    }
  }
  
  private final Kryo kryo= new Kryo();
  private final Class<C> beanClass;
  
  @Override
  public synchronized void encodeToWire(Buffer buffer, C s) {
    log.debug("Encoding {}", s);
    Output out = new Output(new ByteArrayOutputStream());
    kryo.writeObject(out, s);
    
    buffer.appendInt(out.getBuffer().length);
    buffer.appendBytes(out.getBuffer());
  }

  @Override
  public synchronized C decodeFromWire(int pos, Buffer buffer) {
    // My custom message starting from this *pos* of buffer
    int _pos = pos;
    int length = buffer.getInt(_pos);
    log.debug("Decoding buffer of length={}", length);

    // Jump 4 because getInt() == 4 bytes
    byte[] serialized = buffer.getBytes(_pos+=4, _pos+=length);
    C o = kryo.readObject(new Input(serialized), beanClass);
    log.trace("Decoded {}", o);
    return o;
    }

  @Override
  public synchronized C transform(C body) {
    return kryo.copy(body);
  }

  @Override
  public String name() {
    return this.getClass().getSimpleName()+":"+beanClass.getSimpleName();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
  
}
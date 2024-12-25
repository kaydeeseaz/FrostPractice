package dev.demeng.frost.util;

public interface TtlHandler<E> {

  void onExpire(E element);

  long getTimestamp(E element);

}

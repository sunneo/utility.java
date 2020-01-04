package com.example.sharp.io;

public interface ISeekableStream {
	long getLength();

	void setLength(long value) throws Exception;

	long getPosition();

	void setPosition(long value);

	long seek(long offset, SeekOrigin loc) throws Exception;
}

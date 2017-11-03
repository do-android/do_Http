package doext.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import deviceone.org.apache.http.entity.ContentType;
import deviceone.org.apache.http.entity.mime.content.FileBody;
import deviceone.org.apache.http.util.Args;

public class DoCustomFileBody extends FileBody {

	private FileFormListener listener;
	private int transferred; // 上传进度

	public DoCustomFileBody(File file) {
		super(file);
	}

	public DoCustomFileBody(File file, ContentType contentType, String filename) {
		super(file, contentType, filename);
	}

	public DoCustomFileBody(File file, ContentType contentType) {
		super(file, contentType);
	}

	public void setListener(FileFormListener listener) {
		this.listener = listener;
	}

	public interface FileFormListener {
		void transferred(long count, long current , String filename);
	}

	@Override
	public void writeTo(final OutputStream out) throws IOException {
		Args.notNull(out, "Output stream");
		final InputStream in = new FileInputStream(this.getFile());
		try {
			final byte[] tmp = new byte[4096];
			int l;
			while ((l = in.read(tmp)) != -1) {
				out.write(tmp, 0, l);
				if (this.listener != null) {
					this.transferred += l;
					this.listener.transferred(this.getFile().length(), this.transferred , this.getFilename());
				}
			}
			out.flush();
		} finally {
			in.close();
		}
	}

}

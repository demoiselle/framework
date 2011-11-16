package br.gov.frameworkdemoiselle.internal.implementation;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.util.Faces;
import br.gov.frameworkdemoiselle.util.FileRenderer;
import br.gov.frameworkdemoiselle.util.FileRenderer.ContentType;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Faces.class })
public class FileRendererImplTest {

	private Logger logger;

	private FileRenderer renderer;

	private FacesContext facesContext;

	private HttpServletResponse response;

	@Before
	public void before() {
		renderer = new FileRendererImpl();

		logger = PowerMock.createMock(Logger.class);
		Whitebox.setInternalState(renderer, "logger", logger);

		facesContext = PowerMock.createMock(FacesContext.class);
		Whitebox.setInternalState(renderer, "context", facesContext);

		response = PowerMock.createMock(HttpServletResponse.class);
		Whitebox.setInternalState(renderer, "response", response);
	}

	@Test
	public void testRenderBytesFail() {
		byte[] bytes = "Test".getBytes();
		String fileName = "fileName.pdf";

		logger.debug(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();

		IOException exception = new IOException();
		logger.info("Erro na geração do relatório. Incluíndo a exceção de erro em um FacesMessage", exception);
		EasyMock.expectLastCall().anyTimes();

		response.setContentType(ContentType.PDF.getContentType());
		EasyMock.expectLastCall().times(1);

		response.setContentLength(bytes.length);
		EasyMock.expectLastCall().times(1);

		response.setHeader("Content-Disposition", "filename=\"" + fileName + "\"");
		EasyMock.expectLastCall().times(1);

		facesContext.responseComplete();
		EasyMock.expectLastCall().times(1);

		try {
			EasyMock.expect(response.getOutputStream()).andThrow(exception);
		} catch (IOException e) {
			Assert.fail();
		}

		PowerMock.mockStatic(Faces.class);
		Faces.addMessage(exception);

		PowerMock.replayAll();
		renderer.render(bytes, ContentType.PDF, fileName);
		PowerMock.verifyAll();
	}

	@Test
	public void testRenderBytesSuccess() {
		byte[] bytes = "Test".getBytes();
		String fileName = "fileName.pdf";

		logger.debug(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();

		facesContext.responseComplete();
		EasyMock.expectLastCall().times(1);

		response.setContentType(ContentType.PDF.getContentType());
		EasyMock.expectLastCall().times(1);

		response.setContentLength(bytes.length);
		EasyMock.expectLastCall().times(1);

		response.setHeader("Content-Disposition", "filename=\"" + fileName + "\"");
		EasyMock.expectLastCall().times(1);

		ServletOutputStream stream = PowerMock.createMock(ServletOutputStream.class);
		try {
			stream.write(bytes, 0, bytes.length);
			EasyMock.expectLastCall().times(1);

			stream.flush();
			EasyMock.expectLastCall().times(1);

			stream.close();
			EasyMock.expectLastCall().times(1);

			EasyMock.expect(response.getOutputStream()).andReturn(stream).times(3);
		} catch (IOException e) {
			Assert.fail();
		}

		PowerMock.replayAll();
		renderer.render(bytes, ContentType.PDF, fileName);
		PowerMock.verifyAll();
	}

}

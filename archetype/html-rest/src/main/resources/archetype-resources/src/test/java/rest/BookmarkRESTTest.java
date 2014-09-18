package ${package}.rest;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ${package}.entity.Bookmark;
import br.gov.frameworkdemoiselle.HttpViolationException;
import br.gov.frameworkdemoiselle.UnprocessableEntityException;

public class BookmarkRESTTest {

	private static final String BASIC_CREDENTIALS = "Basic " + Base64.encodeBase64String("test:secret".getBytes());

	private CloseableHttpClient client;

	private ObjectMapper mapper;

	private String url;

	@Before
	public void before() throws Exception {
		client = HttpClientBuilder.create().build();
		mapper = new ObjectMapper();

		Configuration config = new PropertiesConfiguration("test.properties");
		url = config.getString("services.url");
	}

	@After
	public void after() throws Exception {
		client.close();
	}

	@Test
	public void findSuccessful() throws ClientProtocolException, IOException {
		HttpGet request;
		CloseableHttpResponse response;

		request = new HttpGet(url + "/bookmark");
		response = client.execute(request);
		response.close();
		List<Bookmark> listAll = mapper.readValue(response.getEntity().getContent(),
				new TypeReference<List<Bookmark>>() {
				});
		assertEquals(SC_OK, response.getStatusLine().getStatusCode());

		String filter = "po";
		request = new HttpGet(url + "/bookmark?q=" + filter);
		response = client.execute(request);
		response.close();
		List<Bookmark> filteredList = mapper.readValue(response.getEntity().getContent(),
				new TypeReference<List<Bookmark>>() {
				});
		assertEquals(SC_OK, response.getStatusLine().getStatusCode());

		for (Bookmark bookmark : filteredList) {
			assertTrue(bookmark.getDescription().toLowerCase().contains(filter)
					|| bookmark.getLink().toLowerCase().contains(filter));
			assertTrue(listAll.contains(bookmark));
		}
	}

	@Test
	public void loadSuccessful() throws Exception {
		Long id = parseEntity(createSample().getEntity(), Long.class);

		HttpGet request = new HttpGet(url + "/bookmark/" + id);
		CloseableHttpResponse response = client.execute(request);
		response.close();
		assertEquals(SC_OK, response.getStatusLine().getStatusCode());

		Bookmark bookmark = parseEntity(response.getEntity(), Bookmark.class);
		assertEquals(Long.valueOf(id), bookmark.getId());
		assertEquals("Google", bookmark.getDescription());
		assertEquals("http://google.com", bookmark.getLink());

		destroySample(id);
	}

	@Test
	public void loadFailed() throws ClientProtocolException, IOException {
		HttpGet request = new HttpGet(url + "/bookmark/99999999");
		CloseableHttpResponse response = client.execute(request);
		response.close();
		assertEquals(SC_NOT_FOUND, response.getStatusLine().getStatusCode());
	}

	@Test
	public void deleteSuccessful() throws Exception {
		Long id = parseEntity(createSample().getEntity(), Long.class);

		HttpDelete request = new HttpDelete(url + "/bookmark/" + id);
		request.addHeader("Authorization", BASIC_CREDENTIALS);
		CloseableHttpResponse response = client.execute(request);
		response.close();
		assertEquals(SC_NO_CONTENT, response.getStatusLine().getStatusCode());
	}

	@Test
	public void deleteFailed() throws Exception {
		HttpDelete request;
		CloseableHttpResponse response;

		Long id = parseEntity(createSample().getEntity(), Long.class);
		request = new HttpDelete(url + "/bookmark/" + id);
		response = client.execute(request);
		response.close();
		assertEquals(SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());
		destroySample(id);

		request = new HttpDelete(url + "/bookmark/99999999");
		request.addHeader("Authorization", BASIC_CREDENTIALS);
		response = client.execute(request);
		response.close();
		assertEquals(SC_NOT_FOUND, response.getStatusLine().getStatusCode());
	}

	@Test
	public void insertSuccessful() throws Exception {
		CloseableHttpResponse response = createSample();
		response.close();

		Long id = parseEntity(response.getEntity(), Long.class);
		assertNotNull(id);

		String expectedLocation = url + "/bookmark/" + id;
		String returnedLocation = response.getHeaders("Location")[0].getValue();
		assertEquals(expectedLocation, returnedLocation);

		HttpGet request = new HttpGet(returnedLocation);
		response = client.execute(request);
		response.close();

		destroySample(id);
	}

	@Test
	public void insertFailed() throws Exception {
		HttpPost request;
		CloseableHttpResponse response;
		Bookmark bookmark;
		Set<UnprocessableEntityException.Violation> violations;
		HttpViolationException expected;

		bookmark = new Bookmark();
		bookmark.setDescription("Google");
		bookmark.setLink("http://google.com");
		request = new HttpPost(url + "/bookmark");
		request.setEntity(createEntity(bookmark));
		request.addHeader("Content-Type", "application/json");
		response = client.execute(request);
		response.close();
		assertEquals(SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());

		bookmark = new Bookmark();
		request = new HttpPost(url + "/bookmark");
		request.setEntity(createEntity(bookmark));
		request.addHeader("Content-Type", "application/json");
		request.addHeader("Authorization", BASIC_CREDENTIALS);
		response = client.execute(request);
		response.close();
		assertEquals(422, response.getStatusLine().getStatusCode());
		violations = mapper.readValue(response.getEntity().getContent(),
				new TypeReference<Set<UnprocessableEntityException.Violation>>() {
				});
		expected = new UnprocessableEntityException();
		expected.addViolation("description", "não pode ser nulo");
		expected.addViolation("link", "não pode ser nulo");
		assertEquals(expected.getViolations(), violations);

		bookmark = new Bookmark();
		bookmark.setDescription("Google");
		bookmark.setLink("http: // google . com");
		request = new HttpPost(url + "/bookmark");
		request.setEntity(createEntity(bookmark));
		request.addHeader("Content-Type", "application/json");
		request.addHeader("Authorization", BASIC_CREDENTIALS);
		response = client.execute(request);
		response.close();
		assertEquals(422, response.getStatusLine().getStatusCode());
		violations = mapper.readValue(response.getEntity().getContent(),
				new TypeReference<Set<UnprocessableEntityException.Violation>>() {
				});
		expected = new UnprocessableEntityException().addViolation("link", "formato inválido");
		assertEquals(expected.getViolations(), violations);

		bookmark = new Bookmark();
		bookmark.setId(Long.valueOf(123456789));
		bookmark.setDescription("Test");
		bookmark.setLink("http://test.com");
		request = new HttpPost(url + "/bookmark");
		request.setEntity(createEntity(bookmark));
		request.addHeader("Content-Type", "application/json");
		request.addHeader("Authorization", BASIC_CREDENTIALS);
		response = client.execute(request);
		response.close();
		assertEquals(SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
	}

	@Test
	public void updateSuccessful() throws Exception {
		HttpRequestBase request;
		CloseableHttpResponse response = createSample();
		response.close();

		Bookmark bookmark = new Bookmark();
		bookmark.setDescription("Google Maps");
		bookmark.setLink("http://maps.google.com");

		Long id = parseEntity(response.getEntity(), Long.class);
		String resourceUrl = url + "/bookmark/" + id;

		request = new HttpPut(resourceUrl);
		((HttpPut) request).setEntity(createEntity(bookmark));
		request.addHeader("Content-Type", "application/json");
		request.addHeader("Authorization", BASIC_CREDENTIALS);
		response = client.execute(request);
		response.close();
		assertEquals(SC_NO_CONTENT, response.getStatusLine().getStatusCode());

		request = new HttpGet(resourceUrl);
		response = client.execute(request);
		response.close();
		Bookmark result = parseEntity(response.getEntity(), Bookmark.class);
		assertEquals(id, result.getId());
		assertEquals(bookmark.getDescription(), result.getDescription());
		assertEquals(bookmark.getLink(), result.getLink());

		destroySample(id);
	}

	@Test
	public void updateFailed() throws Exception {
		HttpPut request;
		CloseableHttpResponse response = createSample();
		response.close();
		Long id = parseEntity(response.getEntity(), Long.class);
		Bookmark bookmark;
		Set<UnprocessableEntityException.Violation> violations;
		HttpViolationException expected;

		bookmark = new Bookmark();
		bookmark.setDescription("Google");
		bookmark.setLink("http://google.com");
		request = new HttpPut(url + "/bookmark/" + id);
		request.setEntity(createEntity(bookmark));
		request.addHeader("Content-Type", "application/json");
		response = client.execute(request);
		response.close();
		assertEquals(SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());

		bookmark = new Bookmark();
		request = new HttpPut(url + "/bookmark/" + id);
		request.setEntity(createEntity(bookmark));
		request.addHeader("Content-Type", "application/json");
		request.addHeader("Authorization", BASIC_CREDENTIALS);
		response = client.execute(request);
		response.close();
		assertEquals(422, response.getStatusLine().getStatusCode());
		violations = mapper.readValue(response.getEntity().getContent(),
				new TypeReference<Set<UnprocessableEntityException.Violation>>() {
				});
		expected = new UnprocessableEntityException();
		expected.addViolation("description", "não pode ser nulo");
		expected.addViolation("link", "não pode ser nulo");
		assertEquals(expected.getViolations(), violations);

		bookmark = new Bookmark();
		bookmark.setDescription("Google");
		bookmark.setLink("http: // google . com");
		request = new HttpPut(url + "/bookmark/" + id);
		request.setEntity(createEntity(bookmark));
		request.addHeader("Content-Type", "application/json");
		request.addHeader("Authorization", BASIC_CREDENTIALS);
		response = client.execute(request);
		response.close();
		assertEquals(422, response.getStatusLine().getStatusCode());
		violations = mapper.readValue(response.getEntity().getContent(),
				new TypeReference<Set<UnprocessableEntityException.Violation>>() {
				});
		expected = new UnprocessableEntityException().addViolation("link", "formato inválido");
		assertEquals(expected.getViolations(), violations);

		bookmark = new Bookmark();
		bookmark.setId(Long.valueOf(123456789));
		bookmark.setDescription("Test");
		bookmark.setLink("http://test.com");
		request = new HttpPut(url + "/bookmark/" + id);
		request.setEntity(createEntity(bookmark));
		request.addHeader("Content-Type", "application/json");
		request.addHeader("Authorization", BASIC_CREDENTIALS);
		response = client.execute(request);
		response.close();
		assertEquals(SC_BAD_REQUEST, response.getStatusLine().getStatusCode());

		destroySample(id);
	}

	private CloseableHttpResponse createSample() throws Exception {
		Bookmark bookmark = new Bookmark();
		bookmark.setDescription("Google");
		bookmark.setLink("http://google.com");

		HttpPost request = new HttpPost(url + "/bookmark");
		request.setEntity(EntityBuilder.create().setText(mapper.writeValueAsString(bookmark)).build());
		request.addHeader("Content-Type", "application/json");
		request.addHeader("Authorization", BASIC_CREDENTIALS);

		CloseableHttpResponse response = client.execute(request);
		response.close();

		return response;
	}

	private void destroySample(Long id) throws Exception {
		HttpDelete request = new HttpDelete(url + "/bookmark/" + id);
		request.addHeader("Authorization", BASIC_CREDENTIALS);
		client.execute(request).close();
	}

	private <T> T parseEntity(HttpEntity entity, Class<T> type) throws Exception {
		return mapper.readValue(entity.getContent(), type);
	}

	private HttpEntity createEntity(Object object) throws Exception {
		return EntityBuilder.create().setText(mapper.writeValueAsString(object)).build();
	}
}

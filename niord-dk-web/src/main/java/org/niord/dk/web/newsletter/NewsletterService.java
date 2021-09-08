/*
 * Copyright 2017 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.niord.dk.web.newsletter;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.json.simple.JSONArray;
import org.niord.core.publication.PublicationSearchParams;
import org.niord.core.publication.PublicationService;
import org.niord.core.publication.vo.PublicationMainType;
import org.niord.core.publication.vo.PublicationStatus;
import org.niord.core.settings.SettingsService;
import org.niord.core.user.Roles;
import org.niord.model.DataFilter;
import org.niord.model.publication.PublicationDescVo;
import org.niord.model.publication.PublicationVo;
import org.niord.model.search.PagedSearchResultVo;
import org.slf4j.Logger;
import twitter4j.HttpResponseCode;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.*;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Interface for sending newsletters
 */
@Path("/newsletter")
@Stateless
@SecurityDomain("keycloak")
@PermitAll
public class NewsletterService{

    @Inject
    private Logger log;

    @Inject
    PublicationService publicationService;

    @Inject
    SettingsService settingsService;

    private static final String baseUriSettingKey = "baseUri";
    private static final String linkChartKey = "linkChart";
    private static final String linkChartEnKey = "linkChartEn";
    private static final String linkFoChartKey = "linkFoChart";
    private static final String linkFoChartEnKey = "linkFoChartEn";
    private static final String newsletterIdKey = "newsletterId";
    private static final String mailListIdKey = "mailListId";
    private static final String newsletterServiceUrlKey = "newsletterServiceUrl ";

    private static String baseUri = "";

    /** Returns all mailing lists that matches the search criteria */
    @POST
    @Path("/send")
    @Produces("application/json")
    @RolesAllowed({Roles.ADMIN, Roles.SYSADMIN})
    @GZIP
    @NoCache
    public String sendNewsletter(@QueryParam("linkChartWeek") String linkChartWeek, @QueryParam("linkChartYear") String linkChartYear, @QueryParam("email") String email, @QueryParam("isTest") boolean isTest) throws IOException, InterruptedException {

        log.info("1");
        baseUri = getBaseUri();
        List<NewsletterPdfDTO> newsletterPdfDTOList = new ArrayList<NewsletterPdfDTO>();

        // Retrieve danish EfS publication ----
        String lang = "da";
        PublicationSearchParams params = new PublicationSearchParams()
                .language(lang)
                .title("EfS uge")
                .statuses(PublicationStatus.ACTIVE)
                .mainType(PublicationMainType.PUBLICATION);
        params.maxSize(4).page(0);

        DataFilter dataFilterDK = DataFilter.get().lang(lang);

        PagedSearchResultVo<PublicationVo> efsPublicationSearchResult = publicationService.searchPublications(params)
                .map(p -> p.toVo(PublicationVo.class, dataFilterDK));

        log.info("2");
        NewsletterPdfDTO efsNewsletterDTO = new NewsletterPdfDTO();
        if(!efsPublicationSearchResult.getData().isEmpty())
        {
            efsPublicationSearchResult.getData().sort(Comparator.comparing(PublicationVo::getUpdated));
            PublicationDescVo publicationDescVo = efsPublicationSearchResult.getData().get(0).getDesc(lang);

            efsNewsletterDTO = mapPublicationDataToNewsletterDTO(publicationDescVo, log);
            newsletterPdfDTOList.add(efsNewsletterDTO);
        }
        else
            efsNewsletterDTO = null;

        // Retrieve danish P&T publication ----
        params.language(lang)
                .title("Aktive P&T uge");

        PagedSearchResultVo<PublicationVo> dkPtPublicationSearchResult = publicationService.searchPublications(params)
                .map(p -> p.toVo(PublicationVo.class, dataFilterDK));

        log.info("3");
        NewsletterPdfDTO dkPtNewsletterDTO = new NewsletterPdfDTO();
        if(!dkPtPublicationSearchResult.getData().isEmpty())
        {
            dkPtPublicationSearchResult.getData().sort(Comparator.comparing(PublicationVo::getUpdated));
            PublicationDescVo publicationDescVo = dkPtPublicationSearchResult.getData().get(0).getDesc(lang);

            dkPtNewsletterDTO = mapPublicationDataToNewsletterDTO(publicationDescVo, log);
            newsletterPdfDTOList.add(dkPtNewsletterDTO);
        }
        else
            dkPtNewsletterDTO = null;

        // Retrieve english NtM publication ---
        lang = "en";
        params.language(lang)
                .title("NtM Week");
        DataFilter dataFilterENG = DataFilter.get().lang(lang);

        PagedSearchResultVo<PublicationVo> enNtMPublicationSearchResult = publicationService.searchPublications(params)
                .map(p -> p.toVo(PublicationVo.class, dataFilterENG));

        log.info("4");
        NewsletterPdfDTO enNtMNewsletterDTO = new NewsletterPdfDTO();
        if(!enNtMPublicationSearchResult.getData().isEmpty())
        {
            enNtMPublicationSearchResult.getData().sort(Comparator.comparing(PublicationVo::getUpdated));
            PublicationDescVo publicationDescVo = enNtMPublicationSearchResult.getData().get(0).getDesc(lang);

            enNtMNewsletterDTO = mapPublicationDataToNewsletterDTO(publicationDescVo, log);
            newsletterPdfDTOList.add(enNtMNewsletterDTO);
        }
        else
            enNtMNewsletterDTO = null;

        // Retrieve english PT publication
        params.language(lang)
                .title("Active P&T Week");

        PagedSearchResultVo<PublicationVo> enPtPublicationSearchResult = publicationService.searchPublications(params)
                .map(p -> p.toVo(PublicationVo.class, dataFilterENG));

        log.info("5");
        NewsletterPdfDTO enPtNewsletterDTO = new NewsletterPdfDTO();
        if(!enPtPublicationSearchResult.getData().isEmpty())
        {
            // Sort list by latest update date.
            enPtPublicationSearchResult.getData().sort(Comparator.comparing(PublicationVo::getUpdated));
            PublicationDescVo publicationDescVo = enPtPublicationSearchResult.getData().get(0).getDesc(lang);

            enPtNewsletterDTO = mapPublicationDataToNewsletterDTO(publicationDescVo, log);
            newsletterPdfDTOList.add(enPtNewsletterDTO);
        }
        else
            enPtNewsletterDTO = null;

        String linkChart = settingsService.getString(linkChartKey);
        String linkChartEn = settingsService.getString(linkChartEnKey);
        String linkFoChart = settingsService.getString(linkFoChartKey);
        String linkFoChartEn = settingsService.getString(linkFoChartEnKey);
        int newsletterId = settingsService.getInteger(newsletterIdKey);
        int mailListId = 0;
        if(!isTest)
        {
            mailListId = settingsService.getInteger(mailListIdKey);
        }
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("mailListId", mailListId);
        json.add("templateId", newsletterId);
        json.add("from", email);

        JsonObjectBuilder dynamicValuesJson = Json.createObjectBuilder();
            dynamicValuesJson.add("linkChart", linkChart);
            dynamicValuesJson.add("linkChartEn", linkChartEn);
            dynamicValuesJson.add("linkFoChart", linkFoChart);
            dynamicValuesJson.add("linkFoChartEn", linkFoChartEn);
            dynamicValuesJson.add("linkChartWeek", linkChartWeek);
            dynamicValuesJson.add("linkChartYear", linkChartYear);
            dynamicValuesJson.add("linkWeek", getCurrentWeek());
            dynamicValuesJson.add("linkYear", getCurrentYear());
            if(efsNewsletterDTO != null)
            {
                dynamicValuesJson.add("linkEfs", efsNewsletterDTO.PdfLink);
            }
            if(dkPtNewsletterDTO != null)
            {
                dynamicValuesJson.add("linkPT", dkPtNewsletterDTO.PdfLink);
            }
            if(enNtMNewsletterDTO != null)
            {
                dynamicValuesJson.add("linkNtm", enNtMNewsletterDTO.PdfLink);
            }
            if(enPtNewsletterDTO != null)
            {
                dynamicValuesJson.add("linkPTen", enPtNewsletterDTO.PdfLink);
            }
            json.add("dynamicValues", dynamicValuesJson);

            // Adds PDF attachments to the Json request
            JsonArrayBuilder jsonArray = Json.createArrayBuilder();
            newsletterPdfDTOList.stream().forEach(dto -> jsonArray.add(Json.createObjectBuilder()
                .add("fileName", dto.PdfName)
                .add("fileContent", dto.PdfContent)
                .add("mediaType", "application/pdf")));
            json.add("attachments", jsonArray);

        JsonObject jsonObject = json.build();
        String jsonString = getJsonString(jsonObject);


        /*
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .header("Content-Type", "application/json")
                .uri(URI.create(getNewsletterSerivceUrl() + (isTest ? "sendtest" : "send")))
                .build();
        HttpResponse result = client.send(request, HttpResponse.BodyHandlers.ofString());
        if(result.statusCode() != HttpResponseCode.OK)
        {
            log.error("Newsletterservice error: " + result.statusCode(), result.body());
        }
        */

        log.error("Calling newsletterservice with " + URI.create(getNewsletterServiceUrl() + (isTest ? "sendtest" : "send")));
        HttpPost postRequest = new HttpPost( URI.create(getNewsletterServiceUrl() + (isTest ? "sendtest" : "send")) );
        postRequest.addHeader("Content-Type", "application/json");
        postRequest.setEntity(new StringEntity(jsonString));
        try ( CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse httpResponse = httpClient.execute(postRequest) ) {

            HttpEntity entity = httpResponse.getEntity();
            JsonObjectBuilder jsonObjectResponse = Json.createObjectBuilder();
            jsonObjectResponse.add("status", httpResponse.getStatusLine().getStatusCode());
            jsonObjectResponse.add("body", EntityUtils.toString(entity));

            return getJsonString(jsonObjectResponse.build());
        }
    }

    private static String getJsonString(JsonObject jsonObject) throws IOException {
        try(Writer writer = new StringWriter()) {
            Json.createWriter(writer).write(jsonObject);
           return writer.toString();
        }
    }
    private static String getPdfContentStringFromURL(URL pdfURL, Logger log)
    {
        String result = "";
        try
        {
            InputStream in = new BufferedInputStream(pdfURL.openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[131072];
            int n = 0;
            while (-1 != (n = in.read(buf))) {
                out.write(buf, 0, n);
            }
            out.close();
            in.close();
            result = Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e)
        {
            log.error(e.getMessage());
        }
        return result;
    }

    private NewsletterPdfDTO mapPublicationDataToNewsletterDTO(PublicationDescVo publication, Logger log) throws MalformedURLException {
        NewsletterPdfDTO dto = new NewsletterPdfDTO();
        // Retrieve PDF info, for URL link as well as attachment data
        dto.PdfName = publication.getFileName();
        dto.PdfLink = baseUri + publication.getLink();
        URL dkPtPdfUrl = new URL(dto.PdfLink);
        dto.PdfContent = getPdfContentStringFromURL(dkPtPdfUrl, log);
        return dto;
    }

    private String getCurrentWeek()
    {
        LocalDate date = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return String.valueOf(date.get(weekFields.weekOfWeekBasedYear()));
    }

    private String getCurrentYear()
    {
        return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    }
    /**
     * Returns the baseUriSettingKey, or null if undefined
     * @return the baseUriSettingKey, or null if undefined
     */
    private String getBaseUri() {
        return settingsService.getString(baseUriSettingKey);
    }
    /**
     * Returns the baseUriSettingKey, or null if undefined
     * @return the baseUriSettingKey, or null if undefined
     */
    private String getNewsletterServiceUrl() {
        return settingsService.getString(newsletterServiceUrlKey);
    }

}

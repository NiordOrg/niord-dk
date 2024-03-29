/*
 * Copyright 2016 Danish Maritime Authority.
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
package org.niord.importer.aton;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.niord.core.aton.AtonNode;
import org.niord.core.aton.vo.AtonNodeVo;
import org.niord.core.aton.vo.AtonOsmVo;
import org.niord.core.batch.BatchService;
import org.niord.core.repo.RepositoryService;
import org.niord.core.sequence.DefaultSequence;
import org.niord.core.sequence.Sequence;
import org.niord.core.sequence.SequenceService;
import org.niord.core.user.Roles;
import org.niord.core.user.UserService;
import org.niord.core.util.WebUtils;
import org.niord.importer.aton.batch.AbstractDkAtonImportProcessor;
import org.slf4j.Logger;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Imports AtoN from Excel sheets.
 */
@Path("/import/atons")
@RequestScoped
@PermitAll
@SuppressWarnings("unused")
public class AtonImportRestService {

    private final static Sequence AFM_SEQUENCE = new DefaultSequence("AFM_ATON_VERSION", 1);

    @Inject
    Logger log;

    @Inject
    BatchService batchService;

    @Inject
    UserService userService;

    @Inject
    SequenceService sequenceService;

    @Inject
    RepositoryService repositoryService;

    /**
     * Imports an uploaded AtoN Excel file
     *
     * @param request the servlet request
     * @return a status
     */
  //  @POST
//    @Path("/upload-xls")
//    @RequestBody(
//            content = @Content(
//                    mediaType = MediaType.MULTIPART_FORM_DATA,
//                    schema = @Schema(implementation = org.niord.core.model.MultipartBody.class)
//            )
//    )
//    @APIResponse(
//            responseCode = "200",
//            content = @Content(
//                    mediaType = MediaType.TEXT_PLAIN,
//                    schema = @Schema(implementation = String.class)
//            )
//    )
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    @Produces(MediaType.TEXT_PLAIN)
//    @Operation(description = "Performs the import operations from the AtoN xls/xlsx data files.")
//    @RolesAllowed(Roles.ADMIN)
    public String importXls(@Parameter(name = "input", hidden = true)
    @QueryParam("name")
    MultipartFormDataInput input) throws Exception {


        // Initialise the form parsing parameters
        final Map<String, Object> uploadForm = WebUtils.getMultipartInputFormParams(input);
        final Map<String, InputStream> files = WebUtils.getMultipartInputFiles(input);
        final StringBuilder txt = new StringBuilder();

        for (Entry<String, InputStream> item : files.entrySet()) {
            String name = item.getKey().toLowerCase();
            InputStream is = item.getValue();
                    

                // AtoN Import
                if (name.startsWith("afmmyndighed_table") && name.endsWith(".xls")) {
                    importAtoN(is, name, txt);

                } else if (name.startsWith("fyr") && name.endsWith(".xls")) {
                    importLights(is, name, txt);

                } else if (name.startsWith("ais") && name.endsWith(".xls")) {
                    importAis(is, name, txt);

                } else if (name.startsWith("dgps") && name.endsWith(".xls")) {
                    importDgps(is, name, txt);

                } else if (name.startsWith("racon") && name.endsWith(".xls")) {
                    importRacons(is, name, txt);
                }
            
        }

        return txt.toString();
    }


    /**
     * Extracts the AtoNs from the Excel sheet
     * @param inputStream the Excel sheet input stream
     * @param fileName the name of the PDF file
     * @param txt a log of the import
     */
    private void importAtoN(InputStream inputStream, String fileName, StringBuilder txt) throws Exception {
        log.info("Extracting AtoNs from Excel sheet " + fileName);

        // Start batch job to import AtoNs
        batchService.startBatchJobWithDataFile(
                "dk-aton-import",
                inputStream,
                fileName,
                initBatchProperties());

        log.info("Started 'dk-aton-import' batch job with file " + fileName);
        txt.append("Started 'dk-aton-import' batch job with file ").append(fileName);
    }


    /**
     * Extracts the lights from the Excel sheet
     * @param inputStream the Excel sheet input stream
     * @param fileName the name of the PDF file
     * @param txt a log of the import
     */
    private void importLights(InputStream inputStream, String fileName, StringBuilder txt) throws Exception {
        log.info("Extracting Lights from Excel sheet " + fileName);

        // Start batch job to import AtoNs
        batchService.startBatchJobWithDataFile(
                "dk-light-import",
                inputStream,
                fileName,
                initBatchProperties());

        log.info("Started 'dk-light-import' batch job with file " + fileName);
        txt.append("Started 'dk-light-import' batch job with file ").append(fileName);
    }

    /**
     * Extracts the AIS from the Excel sheet
     * @param inputStream the Excel sheet input stream
     * @param fileName the name of the PDF file
     * @param txt a log of the import
     */
    private void importAis(InputStream inputStream, String fileName, StringBuilder txt) throws Exception {
        log.info("Extracting AIS from Excel sheet " + fileName);

        // Start batch job to import AtoNs
        batchService.startBatchJobWithDataFile(
                "dk-ais-import",
                inputStream,
                fileName,
                initBatchProperties());

        log.info("Started 'dk-ais-import' batch job with file " + fileName);
        txt.append("Started 'dk-ais-import' batch job with file ").append(fileName);
    }


    /**
     * Extracts the DGPS transmitters from the Excel sheet
     * @param inputStream the Excel sheet input stream
     * @param fileName the name of the PDF file
     * @param txt a log of the import
     */
    private void importDgps(InputStream inputStream, String fileName, StringBuilder txt) throws Exception {
        log.info("Extracting DGPS from Excel sheet " + fileName);

        // Start batch job to import AtoNs
        batchService.startBatchJobWithDataFile(
                "dk-dgps-import",
                inputStream,
                fileName,
                initBatchProperties());

        log.info("Started 'dk-dgps-import' batch job with file " + fileName);
        txt.append("Started 'dk-dgps-import' batch job with file ").append(fileName);
    }


    /**
     * Extracts the RACONs from the Excel sheet
     * @param inputStream the Excel sheet input stream
     * @param fileName the name of the PDF file
     * @param txt a log of the import
     */
    private void importRacons(InputStream inputStream, String fileName, StringBuilder txt) throws Exception {
        log.info("Extracting RACONS from Excel sheet " + fileName);

        // Start batch job to import AtoNs
        batchService.startBatchJobWithDataFile(
                "dk-racon-import",
                inputStream,
                fileName,
                initBatchProperties());

        log.info("Started 'dk-racon-import' batch job with file " + fileName);
        txt.append("Started 'dk-racon-import' batch job with file ").append(fileName);
    }


    /** Initializes the properties to use with the batch data */
    private Map<String, Object> initBatchProperties() {
        int changeset = (int)sequenceService.nextValue(AFM_SEQUENCE);
        Map<String, Object> properties = new HashMap<>();
        properties.put(AbstractDkAtonImportProcessor.CHANGE_SET_PROPERTY, changeset);
        return properties;
    }


    /** Converts the list of AtoNs to an OSM Json representation **/
    private AtonOsmVo toOsm(List<AtonNode> atons) {
        AtonOsmVo osm = new AtonOsmVo();
        osm.setVersion(1.0f);
        osm.setNodes(atons.stream()
                .map(AtonNode::toVo)
                .toArray(AtonNodeVo[]::new));
        return osm;
    }
//
//    /** Prints the result to the command line */
//    private void printResult(List<AtonNode> atons) {
//
//        AtonOsmVo osm = toOsm(atons);
//
//        try {
//            JAXBContext jaxbContext = JAXBContext.newInstance(AtonOsmVo.class);
//            Marshaller marshaller = jaxbContext.createMarshaller();
//            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//            marshaller.marshal(osm, System.out);
//        } catch (JAXBException e) {
//            e.printStackTrace();
//        }
//
//    }

}

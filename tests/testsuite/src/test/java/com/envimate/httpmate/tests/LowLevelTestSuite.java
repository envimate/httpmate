package com.envimate.httpmate.tests;

import org.junit.Test;

import static com.envimate.httpmate.tests.givenwhenthen.Given.givenTheTestHttpMateInstance;
import static com.envimate.httpmate.tests.givenwhenthen.MultipartBuilder.startingWith;
import static com.envimate.httpmate.tests.givenwhenthen.builders.MultipartElement.aFile;
import static com.envimate.httpmate.tests.givenwhenthen.builders.MultipartElement.aFormControl;

public final class LowLevelTestSuite {

    @Test
    public void testBodyOfAPOSTRequest() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("echo_body").viaThePOSTMethod().withTheBody("This is a post request.").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("This is a post request.");
    }

    @Test
    public void testBodyOfAPUTRequest() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("echo_body").viaThePUTMethod().withTheBody("This is a put request.").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("This is a put request.");
    }

    @Test
    public void testBodyOfADELETERequest() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("echo_body").viaTheDELETEMethod().withTheBody("This is a delete request.").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("This is a delete request.");
    }

    @Test
    public void testContentTypeInRequest() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/echo_contenttype").viaTheGETMethod().withAnEmptyBody().withContentType("foobar").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("foobar");
    }

    @Test
    public void testRequestContentTypeIsCaseInsensitive() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/echo_contenttype").viaTheGETMethod().withAnEmptyBody()
                .withTheHeader("CONTENT-TYPE", "foo").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("foo");
    }

    @Test
    public void testFileDownload() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/download").viaTheGETMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/x-msdownload")
                .theResponseBodyWas("download-content")
                .theReponseContainsTheHeader("Content-Disposition", "foo.txt");
    }

    @Test
    public void testMultipartFileUploadWithGET() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/multipart_echo").viaTheGETMethod()
                .withTheMultipartBody(startingWith(aFile("myfile", "asdf.txt", "foooo"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("[{controlname=myfile,filename=asdf.txt,content=foooo}]");
    }

    @Test
    public void testMultipartFileUploadWithPOST() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/multipart_echo").viaThePOSTMethod()
                .withTheMultipartBody(startingWith(aFile("myfile", "asdf.txt", "foooo"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("[{controlname=myfile,filename=asdf.txt,content=foooo}]");
    }

    @Test
    public void testMultipartFileUploadWithPUT() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/multipart_echo").viaThePUTMethod()
                .withTheMultipartBody(startingWith(aFile("myfile", "asdf.txt", "foooo"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("[{controlname=myfile,filename=asdf.txt,content=foooo}]");
    }

    @Test
    public void testMultipartFileUploadWithDELETE() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/multipart_echo").viaTheDELETEMethod()
                .withTheMultipartBody(startingWith(aFile("myfile", "asdf.txt", "foooo"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("[{controlname=myfile,filename=asdf.txt,content=foooo}]");
    }

    @Test
    public void testMultipartWithoutFile() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/multipart_echo").viaThePOSTMethod()
                .withTheMultipartBody(startingWith(aFormControl("foo", "bar"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("[{controlname=foo,content=bar}]");
    }

    @Test
    public void testMultipartWithFieldsBeforeTheFileUpload() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/multipart_echo").viaThePOSTMethod()
                .withTheMultipartBody(startingWith(aFormControl("control1", "content1"))
                        .followedBy(aFormControl("control2", "content2"))
                        .followedBy(aFile("myfile", "asdf.txt", "foooo"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("[{controlname=control1,content=content1}, {controlname=control2,content=content2}, {controlname=myfile,filename=asdf.txt,content=foooo}]");
    }

    @Test
    public void testMultipartFieldsAfterTheFileUpload() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/multipart_echo").viaThePOSTMethod()
                .withTheMultipartBody(startingWith(aFile("myfile", "asdf.txt", "foooooooo"))
                        .followedBy(aFormControl("ignoredcontrol1", "ignoredcontent1"))
                        .followedBy(aFormControl("ignoredcontrol2", "ignoredcontent2"))
                        .followedBy(aFile("myfile", "ignoredfile.txt", "ignoredfilecontent"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("[{controlname=myfile,filename=asdf.txt,content=foooooooo}, {controlname=ignoredcontrol1,content=ignoredcontent1}, {controlname=ignoredcontrol2,content=ignoredcontent2}, {controlname=myfile,filename=ignoredfile.txt,content=ignoredfilecontent}]");
    }

    @Test
    public void testMultipartWithMultipleFiles() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/multipart_echo").viaThePOSTMethod()
                .withTheMultipartBody(startingWith(aFile("file1", "file1.pdf", "content1"))
                        .followedBy(aFile("file2", "file2.pdf", "content2"))
                        .followedBy(aFile("file3", "file3.pdf", "content3"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("[{controlname=file1,filename=file1.pdf,content=content1}, {controlname=file2,filename=file2.pdf,content=content2}, {controlname=file3,filename=file3.pdf,content=content3}]");
    }

    @Test
    public void testAuthenticationByFirstMultipartPart() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/authentication_echo").viaThePOSTMethod()
                .withTheMultipartBody(startingWith(aFormControl("authentication", "username=bob"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("Authenticated as: bob");
    }

    @Test
    public void testAuthorizationBeforeProcessingFile() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("authorized").viaThePOSTMethod().withTheMultipartBody(startingWith(aFormControl("authentication", "username=admin"))
                .followedBy(aFile("file", "file.txt", "content"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("Welcome to the admin section!");
    }

    @Test
    public void testMultipartAndMapmateCanBeUsedSimultaneously() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/multipart_and_mapmate?value1=derp&value2=merp&value3=qwerp").viaThePUTMethod()
                .withTheMultipartBody(startingWith(aFile("file1", "file.txt", "content"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("SUCCESS: Received both a DTO and a file");
    }
}

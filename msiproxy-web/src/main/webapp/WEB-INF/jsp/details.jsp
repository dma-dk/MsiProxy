<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tags/functions" prefix="msi" %>
<html>
<fmt:setLocale value="${lang}"/>
<fmt:bundle basename="MessageDetails">
<head>
    <meta charset="utf-8" />

    <title><fmt:message key="title"/></title>
    <link rel="icon" href="/img/favicon.ico" sizes="16x16 32x32 48x48 64x64" type="image/vnd.microsoft.icon"/>

    <style type="text/css" media="all">

        @page {
            size: a4 portrait;
            margin: 1.5cm 1.5cm;
            padding:0;
        }

        body{
            font-size:11px;
            font-family: Helvetica;
            margin: 0;
            padding:0;
        }

        a {
            color: #000000;
        }

        h4 {
            color: #8f2f7b;
            font-size: 16px;
            margin-bottom: 10px;
            margin-top: 20px;
        }

        .page-break  {
            clear: left;
            display:block;
            page-break-after:always;
            margin: 0;
            padding: 0;
            height: 0;
        }

        table.message-table tr, table.message-table tr {
            page-break-inside: avoid;
        }

        .table-image {
            vertical-align: top;
            padding: 10px;
            border-top: 1px solid lightgray;
        }

        .table-item {
            vertical-align: top;
            width: 100%;
            padding: 10px 10px 10px 40px;
            border-top: 1px solid lightgray;
            font-size:11px;
        }

        .field-name {
            white-space: nowrap;
            vertical-align: top;
            font-style: italic;
            padding-right: 10px;
            text-align: right;
            font-size:11px;
        }

        .field-value {
            vertical-align: top;
            width: 100%;
            font-size:11px;
        }

        .field-value ol {
            padding-left: 0;
        }

    </style>

</head>
<body>

<table class="message-table">
    <c:set var="areaHeadingId" value="${-9999}"/>
    <c:forEach var="msg" items="${messages}">
        <c:set var="areaHeading" value="${msi:areaHeading(msg)}"/>
        <c:if test="${not empty areaHeading and areaHeadingId != areaHeading.id}">
            <c:set var="areaHeadingId" value="${areaHeading.id}"/>
            <tr>
                <td colspan="2"><h4>${msi:areaLineage(areaHeading)}</h4></td>
            </tr>
        </c:if>
        <tr>

            <td class="table-image">
                <img src="/message-map-image/${msg.provider}/${msg.id}.png" width="120" height="120"/>
            </td>
            <td class="table-item">

                <!-- Title line -->
                <c:if test="${msg.originalInformation}">
                    <div>*</div>
                </c:if>
                <div>
                    <strong>
                        <c:if test="${not empty msg.seriesIdentifier.number}">${msg.seriesIdentifier.fullId}.</c:if>
                        <c:if test="${not empty msg.area}">${msi:areaLineage(msg.area)}</c:if>
                        <c:if test="${not empty msg.descs}">
                            <c:if test="${not empty msg.descs[0].vicinity}"> - ${msg.descs[0].vicinity}</c:if>
                            <c:if test="${not empty msg.descs[0].title}"> - ${msg.descs[0].title}</c:if>
                        </c:if>
                    </strong>
                </div>

                <table>

                    <!-- Reference lines -->
                    <c:if test="${not empty msg.references}">
                        <c:forEach var="ref" items="${msg.references}">
                            <tr>
                                <td class="field-name"><fmt:message key="field_reference"/></td>
                                <td class="field-value">
                                    ${ref.seriesIdentifier.fullId}
                                    <c:choose>
                                        <c:when test="${ref.type == 'REPETITION'}"><fmt:message key="ref_repitition"/> </c:when>
                                        <c:when test="${ref.type == 'CANCELLATION'}"><fmt:message key="ref_cancelled"/> </c:when>
                                        <c:when test="${ref.type == 'UPDATE'}"><fmt:message key="ref_updated"/> </c:when>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:if>

                    <!-- Time line -->
                    <tr>
                        <td class="field-name"><fmt:message key="field_time"/></td>
                        <td class="field-value">
                            <c:choose>
                                <c:when test="${not empty msg.descs and not empty msg.descs[0].time}">
                                    ${msg.descs[0].time}
                                </c:when>
                                <c:otherwise>
                                    <fmt:formatDate value="${msg.validFrom}" type="both" dateStyle="medium" timeStyle="medium"/>
                                    <c:if test="${not empty msg.validTo}">
                                        - <fmt:formatDate value="${msg.validTo}" type="both" dateStyle="medium" timeStyle="medium"/>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>

                    <!-- Location line -->
                    <c:if test="${not empty msg.locations}">
                        <tr>
                            <td class="field-name"><fmt:message key="field_location"/></td>
                            <td class="field-value">
                                <c:forEach var="loc" items="${msg.locations}">
                                    <c:if test="${not empty loc.descs}">
                                        <div>${loc.descs[0].description}</div>
                                    </c:if>
                                    <c:forEach var="point" items="${loc.points}">
                                        <div>
                                            ${msi:formatPos(locale, 'DEC', point.lat, point.lon)}<c:if test="${not empty point.descs}">, ${point.descs[0].description}</c:if>
                                        </div>
                                    </c:forEach>
                                </c:forEach>
                            </td>
                        </tr>
                    </c:if>

                    <!-- Details line -->
                    <c:if test="${not empty msg.descs and not empty msg.descs[0].description}">
                        <tr>
                            <td class="field-name"><fmt:message key="field_details"/></td>
                            <td class="field-value">
                                ${msg.descs[0].description}
                            </td>
                        </tr>
                    </c:if>

                    <!-- Note line -->
                    <c:if test="${not empty msg.descs and not empty msg.descs[0].note}">
                        <tr>
                            <td class="field-name"><fmt:message key="field_note"/></td>
                            <td class="field-value">
                                    ${msg.descs[0].note}
                            </td>
                        </tr>
                    </c:if>

                    <!-- Charts line -->
                    <c:if test="${not empty msg.charts}">
                        <tr>
                            <td class="field-name"><fmt:message key="field_charts"/></td>
                            <td class="field-value">
                                <c:forEach var="chart" items="${msg.charts}" varStatus="status">
                                    ${chart.chartNumber}<c:if test="${not empty chart.internationalNumber}"> (INT ${chart.internationalNumber})</c:if><c:if test="${not status.last}">, </c:if>
                                </c:forEach>
                            </td>
                        </tr>
                    </c:if>

                    <!-- Publication line -->
                    <c:if test="${not empty msg.descs and not empty msg.descs[0].publication}">
                        <tr>
                            <td class="field-name"><fmt:message key="field_publication"/></td>
                            <td class="field-value">
                                    ${msg.descs[0].publication}
                            </td>
                        </tr>
                    </c:if>

                    <!-- Source line -->
                    <c:if test="${not empty msg.descs and not empty msg.descs[0].source}">
                        <tr>
                            <td class="field-name" colspan="2">
                                (${msg.descs[0].source})
                            </td>
                        </tr>
                    </c:if>

                </table>

            </td>
        </tr>
    </c:forEach>
</table>
</body>
</fmt:bundle>
</html>


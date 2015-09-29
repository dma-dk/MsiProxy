/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.msiproxy.model.msi;

import javax.persistence.*;
import java.util.Date;

/*
   Contains the information that matches one tweet.

   validTo is specified if available.

 */


@Entity
@Table(name = "tweet")
@NamedQueries({
        @NamedQuery(name = "Tweet.findAll", query = "SELECT n FROM Tweet n"),
        @NamedQuery(name = "Tweet.findByMessageId", query = "SELECT n FROM Tweet n where messageId = :messageId")
})
public class Tweet
{
    Long id;
    Integer messageId;
    Long twitterId;
    String text;
    Date validFrom;
    Date validTo;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public Long getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(Long twitterId) {
        this.twitterId = twitterId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    protected Tweet()
    {
    }

    public Tweet(
            Integer messageId,
            Long twitterId,
            String text,
            Date validFrom,
            Date validTo
    )
    {
        this.messageId=messageId;
        this.twitterId=twitterId;
        this.text=text;
        this.validFrom=validFrom;
        this.validTo=validTo;
    }
}



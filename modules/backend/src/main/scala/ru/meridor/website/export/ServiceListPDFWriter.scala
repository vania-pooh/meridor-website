package ru.meridor.website.export

import ru.meridor.website.export.reader.ServicesList
import java.io.{StringReader, OutputStream}
import com.lowagie.text.{Element, Rectangle, Image, Chunk, PageSize, Font, Paragraph, Document}
import com.lowagie.text.pdf.{BaseFont, PdfPCell, PdfPTable, PdfWriter}
import java.util.Date
import com.lowagie.text.html.simpleparser.HTMLWorker

/**
 * Outputs a list of services to a PDF document
 */
class ServiceListPDFWriter(outputStream: OutputStream) extends Writer[ServicesList] {

  def write(data: ServicesList) = {
    try {
      val document = new Document
      val writer = PdfWriter.getInstance(document, outputStream)
      writer.setFullCompression()
      writer.setLinearPageMode()
      val organization = "ООО \"Меридор\""
      val title = "ООО \"Меридор\": расценки на все виды работ от " + currentDateTime
      document.addAuthor(organization)
      document.addCreator(organization)
      document.addTitle(title)
      document.addSubject(title)
      document.addKeywords("меридор, электромонтажные работы, вызов электрика, светодизайн, освещение, подключение электроприборов")
      document.addCreationDate()
      document.addProducer()
      document.setPageSize(PageSize.A4)
      document.open()
      document.add(aboutSection)
      document.add(servicesTable(data))
      document.close()
      writer.close()
      true
    } catch {
      case e: Exception => false
    }
  }

  private def servicesTable(data: ServicesList): PdfPTable = {
    val quantitiesSpecified = data.quantities.size > 0
    val relativeColumnWidths: Array[Float] = if (quantitiesSpecified)
      Array(0.5f, 0.125f, 0.125f, 0.125f, 0.125f)
      else Array(0.6f, 0.2f, 0.2f)
    val table = new PdfPTable(relativeColumnWidths)
    renderData(table, data, headerLevel = 1, quantitiesSpecified, isTopLevel = true)
    table
  }

  private def renderData(table: PdfPTable, data: ServicesList, headerLevel: Int, quantitiesSpecified: Boolean, isTopLevel: Boolean = false): Double = {
    var totalValue = 0d
    val numCols = if (quantitiesSpecified) 5 else 3
    if (isTopLevel){
      val boldFont = new Font(baseFont, 10, Font.BOLD)
      table.addCell(new PdfPCell(text("Наименование", boldFont)))
      table.addCell(new PdfPCell(text("Ед. изм.", boldFont)))
      table.addCell(new PdfPCell(text("Цена за ед.изм.", boldFont)))
      if (quantitiesSpecified){
        table.addCell(new PdfPCell(text("Объем", boldFont)))
        table.addCell(new PdfPCell(text("Полная стоимость", boldFont)))
      }
    }
    data.servicesData foreach {
      el => {
        var groupValue = 0d
        val groupName = el._1.displayName
        val servicesData = el._2
        if (servicesData.size > 0){
          table.addCell(new PdfPCell(text(groupName, headerFont(headerLevel))){
            {
              setColspan(numCols)
            }
          })
        }
        servicesData.services foreach {
          s => {
            table.addCell(text(s.displayName))
            table.addCell(text(s.unitOfMeasure.displayName))
            table.addCell(text(formatPrice(s.price)))
            if (quantitiesSpecified){
              val quantity = data.quantity(s)
              val value = quantity * s.price
              groupValue += value
              table.addCell(text(quantity.toString))
              table.addCell(text(formatPrice(value)))
            }
          }
        }
        groupValue += renderData(table, ServicesList(servicesData.childGroupsData, data.quantities), headerLevel + 1, quantitiesSpecified)
        if (quantitiesSpecified){
          val totalValueText = "Итого по разделу \"" + groupName + "\": " + formatPrice(groupValue) + " руб."
          table.addCell(new PdfPCell(text(totalValueText, headerFont(headerLevel + 1))){
            {
              setColspan(numCols)
              setHorizontalAlignment(Element.ALIGN_RIGHT)
            }
          })
        }
        totalValue += groupValue
      }
    }
    if (isTopLevel && quantitiesSpecified){
      table.addCell(new PdfPCell(text("Итого: " + formatPrice(totalValue) + " руб.", headerFont(headerLevel))){
        {
          setColspan(numCols)
          setHorizontalAlignment(Element.ALIGN_RIGHT)
        }
      })
    }
    totalValue
  }

  private def aboutSection: PdfPTable = {
    val table = new PdfPTable(Array(0.5f, 0.5f))
    table.addCell(new PdfPCell(image("/pdf/logo.png")){
      {
        setBorder(Rectangle.NO_BORDER)
      }
    })
    table.addCell(new PdfPCell(){
      {
        setBorder(Rectangle.NO_BORDER)
        setVerticalAlignment(Element.ALIGN_MIDDLE)
        addElement(text("ООО \"Меридор\"\n", new Font(baseFont, 18, Font.BOLD)))
        addElement(text("(812) 929-8996\n", new Font(baseFont, 16, Font.BOLD)))
        val timeFont = new Font(baseFont, 14)
        addElement(text("Пн-Пт: с 10 до 20\n", timeFont))
        addElement(text("Сб, Вс: с 10 до 18\n", timeFont))
      }
    })
    table
  }

  /**
   * Prepare a paragraph from raw text and optionally replace some text tags like &lt;sup&gt; or &lt;sub&gt;
   * @param s
   * @return
   */
  private def text(s: String, font: Font = defaultFont, smallerFont: Font = smallerFont): Paragraph = {
    import scala.collection.JavaConversions._
    try {
      val strReader = new StringReader(s)
      val list = HTMLWorker.parseToList(strReader, null)
      if (list.size() == 1 && list.get(0).isInstanceOf[Paragraph]){
        val oldParagraph = list.get(0).asInstanceOf[Paragraph]
        val p = new Paragraph(oldParagraph){
          {
            setFont(font)
          }
        }
        val chunks = p.getChunks.toList.asInstanceOf[List[Chunk]]
        for (chunk <- chunks){
          val chunkFont = if (chunk.getAttributes.containsKey(Chunk.SUBSUPSCRIPT))
            smallerFont
            else font
          chunk.setFont(chunkFont)
        }
        p
      } else new Paragraph(s, font)
    } catch {
      case e: Exception => new Paragraph(s, font)
    }

  }

  private def image(fileName: String): Image = {
    val img = Image.getInstance(getClass.getResource(fileName).toString)
    img.setCompressionLevel(0)
    img.setAlignment(Image.LEFT)
    img.scalePercent(100)
    img
  }

  private val baseFont = {
    val fontPath = getClass.getResource("/pdf/Tahoma.ttf").toString
    BaseFont.createFont(fontPath, "Cp1251", BaseFont.NOT_EMBEDDED)
  }

  private val defaultFont = new Font(baseFont, 10)

  private val smallerFont = new Font(baseFont, 6)

  private def headerFont(level: Int = 1) = {
    val size = level match {
      case 1 => 14
      case 2 => 12
      case 3 => 10
      case _ => 8
    }
    new Font(baseFont, size, Font.BOLD)
  }

  private val currentDateTime = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date())

  private def formatPrice(price: Double) = new java.text.DecimalFormat("#.##").format(price)

}

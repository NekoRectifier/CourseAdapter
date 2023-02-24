package main.java.parser

import bean.Course
import parser.Parser
import org.jsoup.Jsoup
import java.lang.NumberFormatException

class HUATParser(source: String) : Parser(source) {

    var course = ArrayList<Course>()

    private fun isNumber(num: String) :Boolean {
        return try {
            num.toInt() == num.toInt()
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun infoExtract(
        rawInfo: String,
        colNum: Int,
        rowNum: Int
    ) {
        val formattedData = ArrayList<String>()
        val rawArray = rawInfo.split(" ")

        formattedData.add(0, rawArray[0])

        var hIndex = -1
        if (rawArray.indexOf("2H") != -1) {
            hIndex = rawArray.indexOf("2H")
        } else if (rawArray.indexOf("1H") != -1) {
            hIndex = rawArray.indexOf("1H")
        } else {
//            hIndex = 2
            // may have problem
        }

        var teacherNames = ""
        // Check if there's a teacher
        if (1 != hIndex) {
            for (nameIndex in 1 until hIndex) {
                teacherNames = teacherNames + rawArray[nameIndex] + " "
            }
            formattedData.add(1, teacherNames)
        } else {
            formattedData.add(1, "未知教师")
        }

        var roomNumber: String = rawArray[hIndex + 1]
        if (!isNumber(roomNumber)) {
            roomNumber = "未知教室"
        } else if (isNumber(roomNumber ) && roomNumber.length != 4) {
            roomNumber = "未知教室"
        }

        formattedData.add(2, roomNumber)

        val startWeek: Int
        val endWeek: Int
        if (roomNumber == "未知教室") {
            val size = rawArray.size
            startWeek = rawArray[size - 3].toInt()
            endWeek = rawArray[size - 1].substringBefore("周").toInt()
        } else {
            startWeek = rawArray[hIndex + 2].toInt()
            endWeek = rawArray[hIndex + 4].substringBefore("周").toInt()
        }
//        print(formattedData)

        val startNode: Int
        val endNode: Int
        if (rowNum != 4) {
            endNode = 2 * (colNum + 1)
            startNode = endNode - 1
        } else {
            endNode = 2 * (colNum + 1) + 1
            startNode = endNode - 2
        }

        course.add(
            Course(
                name = rawArray[0],
                day = colNum,
                room = roomNumber,
                teacher = teacherNames,
                startNode = startNode,
                endNode = endNode,
                startWeek = startWeek,
                endWeek = endWeek,
                type = 0
                )
        )
    }

    override fun generateCourseList(): List<Course> {
        val document = Jsoup.parse(source)
        var baseTable = document.getElementById("ctl00_ContentPlaceHolder1_CourseTable")

        if (baseTable == null) {
            baseTable = document.getElementById("CourseTable")
        }

        var colNum = 1
        val rows = baseTable.select("[valign=\"middle\"]")

        for ((rowNum, row) in rows.withIndex()) {
            val rowElements = row.select("[bgcolor]")
            for (item in rowElements) {
                infoExtract(item.child(0).text(), rowNum, colNum)
                colNum++
            }
            colNum = 1
        }
        return course
    }
}
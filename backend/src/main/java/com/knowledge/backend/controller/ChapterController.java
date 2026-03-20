package com.knowledge.backend.controller;

import com.knowledge.backend.common.Result;
import com.knowledge.backend.entity.Chapter;
import com.knowledge.backend.entity.ColumnInfo;
import com.knowledge.backend.service.ChapterService;
import com.knowledge.backend.service.ColumnInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chapter")
public class ChapterController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private ColumnInfoService columnInfoService;

    @PostMapping("/create")
    public Result<Chapter> create(@RequestBody Chapter chapter, @RequestAttribute("userId") Long userId) {
        ColumnInfo column = columnInfoService.getById(chapter.getColumnId());
        if (column == null || !column.getCreatorId().equals(userId)) {
            return Result.error(403, "无权管理该专栏的章节");
        }
        chapterService.save(chapter);
        return Result.success(chapter);
    }

    @GetMapping("/list/{columnId}")
    public Result<List<Chapter>> listByColumn(@PathVariable("columnId") Long columnId) {
        List<Chapter> list = chapterService.lambdaQuery()
                .eq(Chapter::getColumnId, columnId)
                .orderByAsc(Chapter::getSortOrder)
                .list();
        return Result.success(list);
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody Chapter chapter, @RequestAttribute("userId") Long userId) {
        Chapter old = chapterService.getById(chapter.getId());
        if (old == null) return Result.error(404, "章节不存在");
        
        ColumnInfo column = columnInfoService.getById(old.getColumnId());
        if (column == null || !column.getCreatorId().equals(userId)) {
            return Result.error(403, "无权修改该章节");
        }
        return Result.success(chapterService.updateById(chapter));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable("id") Long id, @RequestAttribute("userId") Long userId) {
        Chapter chapter = chapterService.getById(id);
        if (chapter == null) return Result.success(true);
        
        ColumnInfo column = columnInfoService.getById(chapter.getColumnId());
        if (column == null || !column.getCreatorId().equals(userId)) {
            return Result.error(403, "无权删除该章节");
        }
        // TODO: 可选逻辑，如果章节下有文章，是否允许删除？
        return Result.success(chapterService.removeById(id));
    }
}

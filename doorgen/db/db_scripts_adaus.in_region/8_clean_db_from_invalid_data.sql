DELETE FROM pages WHERE id IN (
	SELECT t.id FROM (
		SELECT DISTINCT p.id FROM door_keys k LEFT JOIN pages p ON k.id = p.key_id LEFT JOIN page_content pc ON p.id = pc.page_id LEFT JOIN content_detail cd ON cd.page_content_id = pc.id WHERE (cd.id IS NULL || pc.id IS NULL) AND k.key_value <> '/'
	) t
	WHERE t.id IS NOT NULL
)
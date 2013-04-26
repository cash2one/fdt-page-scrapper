				<div id="content">
					<div class="sidebar">
						<span>Last Requests:</span>
						<ul>
							<?php echo $T->getQueries(5, 'li', 'videos'); ?>
						</ul>
					</div><!--
				 --><div class="main">
						<div class="videos">
							<span>Видео по запросу: <b><?php echo $T->getQuery(); ?></b><span>
							<iframe width="600" height="440" src="<?php echo $T->getVideo(0)->url; ?>" frameborder="0" allowfullscreen></iframe>
							<?php echo $T->getVideo(1, 'p')->frame; ?>
							<?php echo $T->getVideo(2, 'p')->popup; ?>
						</div>
					</div>
				</div>
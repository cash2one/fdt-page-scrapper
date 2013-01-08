				<div id="content">
					<div class="sidebar">
						<span>Last Requests:</span>
						<ul>
							<?php echo $T->getQueries(5, 'li', 'images'); ?>
						</ul>
					</div><!--
				 --><div class="main">
						<span>Изображения по запросу: <b><?php echo $T->getQuery(); ?></b></span>
						<div class="images">
							<?php echo $T->getImage(0, 'p')->frame; ?>

							<p class="image">
								<a href="#i1" rel="prettyPhoto[i]">
									<?php echo $T->getImage(1)->small; ?>
								</a>
							</p>
							<div id="i1" style="display:none;"><?php echo $T->getImage(1)->large; ?></div>

							<p class="image">
								<a href="#i2" rel="prettyPhoto[i]">
									<?php echo $T->getImage(2)->small; ?>
								</a>
							</p>
							<div id="i2" style="display:none;"><?php echo $T->getImage(2)->large; ?></div>

							<?php echo $T->getImage(3, 'p')->frame; ?>

							<?php echo $T->getImage(4, 'p')->frame; ?>

							<p class="image">
								<a href="#i5" rel="prettyPhoto[i]">
									<?php echo $T->getImage(5)->small; ?>
								</a>
							</p>
							<div id="i5" style="display:none;"><?php echo $T->getImage(5)->large; ?></div>

							<p class="image">
								<a href="#i6" rel="prettyPhoto[i]">
									<?php echo $T->getImage(6)->small; ?>
								</a>
							</p>
							<div id="i6" style="display:none;"><?php echo $T->getImage(6)->large; ?></div>

							<?php echo $T->getImage(7, 'p')->frame; ?>
						</div>
					</div>
				</div>